package digital.twin;

import digital.twin.attributes.AttributeSpecification;
import digital.twin.attributes.AttributeType;
import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.sys.MObjectState;
import redis.clients.jedis.Jedis;
import utils.DTLogger;
import utils.StringUtils;
import utils.USEUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class that retrieves all instances of a USE model class and serializes them for storage in the data lake.
 */
public abstract class OutputManager {

    protected static final String SNAPSHOT_ID = "snapshotId";

    protected final AttributeSpecification attributeSpecification;
    private final String channel;
    private final String retrievedClass;
    private final String objectIdPrefix;

    /**
     * Default constructor. Constructors from subclasses must set the type of the attributes to serialize
     * using the attributeSpecification instance.
     * @param channel The channel this OutputManager is created from.
     * @param retrievedClass The class whose instances to retrieve and serialize.
     * @param objectIdPrefix A prefix to be appended to the identifiers of all serialized instances.
     */
    public OutputManager(String channel, String retrievedClass, String objectIdPrefix) {
        attributeSpecification = new AttributeSpecification();
        attributeSpecification.set("twinId", AttributeType.STRING);
        attributeSpecification.set("executionId", AttributeType.STRING);
        attributeSpecification.set("timestamp", AttributeType.NUMBER);
        this.channel = channel;
        this.retrievedClass = retrievedClass;
        this.objectIdPrefix = objectIdPrefix;
    }

    /**
     * Returns the channel this OutputManager is created from.
     * @return The channel this OutputManager is associated with.
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Retrieves the objects of class <i>retrievedClass</i> from the currently displayed object diagram.
     * @param api USE system API instance to interact with the currently displayed object diagram.
     * @return The list of objects available in the currently displayed object diagram.
     */
    public List<MObjectState> getObjectsFromModel(UseSystemApi api) {
        return USEUtils.getObjectsOfClass(api, retrievedClass);
    }

    /**
     * Saves all objects to the data lake.
     * @param api The USE system API instance to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @throws UseApiException Any error related to the USE API.
     */
    public abstract void saveObjectsToDataLake(UseSystemApi api, Jedis jedis) throws UseApiException;

    /**
     * Adds a search register to the database to maintain a list of all states of an object for a digital twin
     * and an execution id.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @param twinIdExecutionId The twin ID and execution ID of the object: "[objectIdPrefix]:[twinId]:[executionId]".
     * @param attributeName The name of the attribute to save.
     * @param type The type of the attribute to save.
     * @param value The value to save, as a USE value.
     * @param objectId The key of the object to be able to be retrieved.
     */
    protected void addSearchRegister(
            Jedis jedis, String twinIdExecutionId, String attributeName,
            AttributeType type, String value, String objectId) {
        String key = twinIdExecutionId + ":" + attributeName.toUpperCase() + "_LIST";
        double score;
        switch (type) {

            case NUMBER:
                score = Double.parseDouble(value.replace("'", ""));
                jedis.zadd(key, score, objectId);
                break;

            case BOOLEAN:
                score = Boolean.parseBoolean(value) ? 1 : 0;
                jedis.zadd(key, score, objectId);
                break;

        }
    }

    /**
     * Auxiliary method to store the object in the database, extracted from the diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @param snapshot The object to store.
     */
    protected void saveOneObject(Jedis jedis, MObjectState snapshot) {
        Map<String, String> armValues = new HashMap<>();

        // Generate the object identifier
        String objectId = generateOutputObjectId(snapshot);
        armValues.put(SNAPSHOT_ID, objectId);

        // Get object ID without timestamp, [objectIdPrefix]:[twinId]:[executionId]
        String objectIdNoStamp = objectId.substring(0, objectId.lastIndexOf(":"));

        DTLogger.info(getChannel(), "---");
        for (String attr : attributeSpecification.attributeNames()) {

            AttributeType attrType = attributeSpecification.typeOf(attr);
            int multiplicity = attributeSpecification.multiplicityOf(attr);
            String attrValue = USEUtils.getAttributeAsString(snapshot, attr);
            if (attrValue != null) {
                DTLogger.info(getChannel(), attr + ": " + attrValue);
                if (multiplicity > 1) {
                    // A sequence of values
                    String[] values = extractValuesFromCollection(attrValue, attrType);
                    if (values.length == multiplicity) {
                        for (int i = 1; i <= multiplicity; i++) {
                            String attrI = attr + "_" + i;
                            armValues.put(attrI, values[i - 1]);
                            addSearchRegister(jedis, objectIdNoStamp, attrI, attrType, values[i - 1], objectId);
                        }
                    } else {
                        DTLogger.warn(getChannel(), "Attribute " + attr + " has " + values.length
                                + " value(s), but we need " + multiplicity);
                    }
                } else {
                    // A single value
                    armValues.put(attr, attrType.toRedisString(attrValue));
                    addSearchRegister(jedis, objectId, attr, attrType, attrValue, objectId);
                }
            } else {
                DTLogger.warn(getChannel(), "Attribute " + attr + " not found in class " + retrievedClass);
            }
        }

        // Save the object
        DTLogger.info(getChannel(), "Saved snapshot: " + objectId);
        DTLogger.info(getChannel(), "---");
        jedis.hset(objectId, armValues);

        // Add to a set with references to all saved instances
        jedis.zadd(objectIdPrefix, 0, objectId);
    }

    /**
     * Generates and returns an identifier for an object to be stored in the data lake.
     * @param objstate The object state to generate the identifier from.
     * @return The identifier for the object: "[objectIdPrefix]:[twinId]:[executionId]:[timestamp]".
     */
    private String generateOutputObjectId(MObjectState objstate) {
        String twinId = USEUtils.getAttributeAsString(objstate, "twinId");
        String executionId = USEUtils.getAttributeAsString(objstate, "executionId");
        String timestamp = USEUtils.getAttributeAsString(objstate, "timestamp");
        assert twinId != null;
        assert executionId != null;
        return objectIdPrefix
            + ":" + StringUtils.removeQuotes(twinId)
            + ":" + StringUtils.removeQuotes(executionId)
            + ":" + timestamp;
    }

    /**
     * Converts an USE collection value to an array of values.
     * @param collection The collection value to convert.
     * @return An array of strings containing each value in the collection.
     */
    private String[] extractValuesFromCollection(String collection, AttributeType baseType) {
        collection = collection.replaceAll("(?:Set|Bag|OrderedSet|Sequence)\\{(.*)}", "$1");
        String[] result = collection.split(",");
        for (int i = 0; i < result.length; i++) {
            result[i] = baseType.toRedisString(result[i]);
        }
        return result;
    }

}
