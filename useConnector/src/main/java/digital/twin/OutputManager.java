package digital.twin;

import org.tzi.use.api.UseApiException;
import org.tzi.use.uml.sys.MObjectState;
import redis.clients.jedis.Jedis;
import utils.DTLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class that retrieves all instances of a USE model class and serializes them for storage in the data lake.
 */
public abstract class OutputManager {

    protected static final String IS_PROCESSED = "isProcessed";
    protected static final String WHEN_PROCESSED = "whenProcessed";
    protected static final String TIMESTAMP = "timestamp";

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
        attributeSpecification.set(TIMESTAMP, AttributeType.INTEGER);
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
        List<MObjectState> unprocessedObjects = getUnprocessedModelObjects();
        for (MObjectState objstate : unprocessedObjects) {
            saveOneObject(jedis, objstate);
        }
    }

    /**
     * Auxiliary method to store the object in the database, extracted from the diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @param objstate The object to store.
     */
    private synchronized void saveOneObject(Jedis jedis, MObjectState objstate) {
        Map<String, String> armValues = new HashMap<>();

        // Generate the object identifier
        String objectId = getObjectId(objstate);
        String objectTypeAndId = objectType + ":" + objectId;

        for (String attr : attributeSpecification.attributeNames()) {
            AttributeType attrType = attributeSpecification.typeOf(attr);
            int multiplicity = attributeSpecification.multiplicityOf(attr);
            String attrValue = useApi.getAttributeAsString(objstate, attr);
            if (attrValue != null) {
                if (multiplicity > 1) {
                    // A sequence of values
                    String[] values = extractValuesFromCollection(attrValue, attrType);
                    if (values.length == multiplicity) {
                        for (int i = 1; i <= multiplicity; i++) {
                            String attrI = attr + "_" + i;
                            String attrvalueI = attrType.fromUseToRedisString(values[i - 1]);
                            armValues.put(attrI,attrvalueI);
                            addAttributeQueryRegisters(jedis, objectTypeAndId, attrI, attrType, attrvalueI);
                        }
                    } else {
                        DTLogger.warn(getChannel(),
                                "Error saving output object " + objectTypeAndId + ": "
                                + "attribute " + attr + " has " + values.length
                                + " value(s), but we need " + multiplicity);
                    }
                } else {
                    // A single value
                    attrValue = attrType.fromUseToRedisString(attrValue);
                    armValues.put(attr, attrValue);
                    addAttributeQueryRegisters(jedis, objectTypeAndId, attr, attrType, attrValue);
                }
            } else {
                DTLogger.warn(getChannel(),
                        "Error saving output object " + objectTypeAndId + ": "
                                + "attribute " + attr + " not found in class " + retrievedClass);
            }
        }

        // Save the object
        jedis.hset(objectTypeAndId, armValues);
        DTLogger.info(getChannel(), "Saved output object: " + objectTypeAndId);

        // Mark object as processed
        int time = useApi.getCurrentTime();
        jedis.zadd(objectType + "_PROCESSED", getObjectScore(objstate), objectTypeAndId);
        jedis.hset(objectTypeAndId, WHEN_PROCESSED, time + "");
        useApi.setAttribute(objstate, IS_PROCESSED, true);
        useApi.setAttribute(objstate, WHEN_PROCESSED, time);

        // Add registers for other queries
        addObjectQueryRegisters(jedis, objectTypeAndId, armValues);

        // Clean up
        try {
            cleanUpModel(objstate);
        } catch (Exception ex) {
            DTLogger.error(getChannel(), "Could not clean up model:");
            ex.printStackTrace();
        }
    }

    /**
     * Removes processed objects from the Data Lake.
     * @param objstate The object state that has been processed.
     */
    protected abstract void cleanUpModel(MObjectState objstate) throws UseApiException;

    /**
     * Adds registers to the data lake each time an object is processed to make queries possible.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @param objectTypeAndId The ID of the object to generate the registers for.
     * @param values The values of the object to generate the registers for.
     */
    protected abstract void addObjectQueryRegisters(
            Jedis jedis, String objectTypeAndId, Map<String, String> values);

    /**
     * Adds registers to the data lake each time an attribute is processed to make queries possible.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @param objectTypeAndId The ID of the object to generate the registers for.
     * @param attributeName The name of the attribute to generate the registers for.
     * @param type The type of the attribute to generate the registers for.
     * @param attributeValue The value of the attribute to generate the registers for.
     */
    protected abstract void addAttributeQueryRegisters(
            Jedis jedis, String objectTypeAndId, String attributeName,
            AttributeType type, String attributeValue);

    /**
     * Generates and returns an identifier for an object to be stored in the data lake.
     * @param objstate The object state to generate the identifier from.
     * @return The identifier for the object.
     */
    protected abstract String getObjectId(MObjectState objstate);

    /**
     * Returns the score to use for an object to be stored in the data lake. Used to update
     * the processed object set.
     * @param objstate The object state to return the score from.
     * @return The score for the object.
     */
    protected abstract double getObjectScore(MObjectState objstate);

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
            result[i] = baseType.fromUseToRedisString(result[i]);
        }
        return result;
    }

}
