package digital.twin;

import digital.twin.attributes.AttributeSpecification;
import digital.twin.attributes.AttributeType;
import org.tzi.use.uml.sys.MObjectState;
import redis.clients.jedis.Jedis;
import utils.DTLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class that retrieves all instances of a USE model class and serializes them for storage in the data lake.
 */
public abstract class OutputManager {

    protected static final String SNAPSHOT_ID = "snapshotId";
    protected static final String IS_PROCESSED = "isProcessed";
    protected static final String WHEN_PROCESSED = "whenProcessed";
    private static final ReentrantLock logMutex = new ReentrantLock();

    protected final AttributeSpecification attributeSpecification;
    protected final DTUseFacade useApi;
    private final String channel;
    private final String retrievedClass;
    private final String objectType;

    /**
     * Default constructor. Constructors from subclasses must set the type of the attributes to serialize
     * using the attributeSpecification instance.
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     * @param channel The channel this OutputManager is created from.
     * @param retrievedClass The class whose instances to retrieve and serialize.
     * @param objectType A prefix to be appended to the identifiers of all serialized instances
     *      to identify their type.
     */
    public OutputManager(DTUseFacade useApi, String channel, String retrievedClass, String objectType) {
        attributeSpecification = new AttributeSpecification();
        attributeSpecification.set("timestamp", AttributeType.INTEGER);
        this.useApi = useApi;
        this.channel = channel;
        this.retrievedClass = retrievedClass;
        this.objectType = objectType;
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
     * @return The list of objects available in the currently displayed object diagram.
     */
    public List<MObjectState> getUnprocessedModelObjects() {
        List<MObjectState> result = useApi.getObjectsOfClass(retrievedClass);
        result.removeIf(obj -> useApi.getBooleanAttribute(obj, IS_PROCESSED));
        return result;
    }

    /**
     * Saves all the objects in the currently displayed object diagram to the data lake.
     * @param jedis An instance of the Jedis client to access the data lake.
     */
    public void saveObjectsToDataLake(Jedis jedis) {
        useApi.updateDerivedValues();
        List<MObjectState> unprocessedCommands = getUnprocessedModelObjects();
        for (MObjectState command : unprocessedCommands) {
            try {
                logMutex.lock();
                saveOneObject(jedis, command);
            } finally {
                logMutex.unlock();
            }
        }
    }

    /**
     * Auxiliary method to store the object in the database, extracted from the diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @param snapshot The object to store.
     */
    private void saveOneObject(Jedis jedis, MObjectState snapshot) {
        Map<String, String> armValues = new HashMap<>();

        // Generate the object identifier
        String objectId = getObjectId(snapshot);
        int timestamp = useApi.getIntegerAttribute(snapshot, "timestamp");
        String objectTypeAndId = objectType + ":" + objectId;
        String fullSnapshotId = objectTypeAndId + ":" + timestamp;
        armValues.put(SNAPSHOT_ID, fullSnapshotId);

        DTLogger.info(getChannel(), "Saving output object: " + fullSnapshotId);
        for (String attr : attributeSpecification.attributeNames()) {
            AttributeType attrType = attributeSpecification.typeOf(attr);
            int multiplicity = attributeSpecification.multiplicityOf(attr);
            String attrValue = useApi.getAttributeAsString(snapshot, attr);
            if (attrValue != null) {
                DTLogger.info(getChannel(), "  " + attr + ": " + attrValue);
                if (multiplicity > 1) {
                    // A sequence of values
                    String[] values = extractValuesFromCollection(attrValue, attrType);
                    if (values.length == multiplicity) {
                        for (int i = 1; i <= multiplicity; i++) {
                            String attrI = attr + "_" + i;
                            armValues.put(attrI, values[i - 1]);
                            addSearchRegister(jedis, objectTypeAndId, attrI, attrType, values[i - 1], fullSnapshotId);
                        }
                    } else {
                        DTLogger.warn(getChannel(), "  Attribute " + attr + " has " + values.length
                                + " value(s), but we need " + multiplicity);
                    }
                } else {
                    // A single value
                    armValues.put(attr, attrType.toRedisString(attrValue));
                    addSearchRegister(jedis, objectTypeAndId, attr, attrType, attrValue, fullSnapshotId);
                }
            } else {
                DTLogger.warn(getChannel(), "  Attribute " + attr + " not found in class " + retrievedClass);
            }
        }

        // Save the object
        jedis.hset(fullSnapshotId, armValues);

        // Mark object as processed
        jedis.zadd(objectType + "_PROCESSED", 0, fullSnapshotId);
        useApi.setAttribute(snapshot, IS_PROCESSED, true);
        useApi.setAttribute(snapshot, WHEN_PROCESSED, useApi.getCurrentTime());
    }

    /**
     * Adds a search register to the database to maintain a list of all states of an object.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @param objectTypeAndId The ID of the object to generate the search register for.
     * @param attributeName The name of the attribute to save.
     * @param type The type of the attribute to save.
     * @param value The value to save, as a USE value.
     * @param objectId The key of the object to be able to be retrieved.
     */
    protected void addSearchRegister(
            Jedis jedis, String objectTypeAndId, String attributeName,
            AttributeType type, String value, String objectId) {
        String key = objectTypeAndId + ":" + attributeName + "_HISTORY";
        double score;
        switch (type) {

            case INTEGER:
            case REAL:
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
     * Generates and returns an identifier for an object to be stored in the data lake.
     * @param objstate The object state to generate the identifier from.
     * @return The identifier for the object.
     */
    protected abstract String getObjectId(MObjectState objstate);

    /**
     * Converts an USE collection value to an array of values.
     * @param collection The collection value to convert.
     * @param baseType Type of each element in the collection.
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
