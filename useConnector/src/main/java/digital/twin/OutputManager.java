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
 *
 * @author Paula Muñoz - University of Málaga
 *
 */
public abstract class OutputManager {

    protected static final String SNAPSHOT_ID = "snapshotId";

    protected final AttributeSpecification attributeSpecification;
    protected String retrievedClass;
    protected String processedObjectsSetIdentifier;
    private String channel;

    public OutputManager() {
        attributeSpecification = new AttributeSpecification();
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Retrieves the objects of class retrievedClass from the currently displayed object diagram.
     *
     * @param api USE system API instance to interact with the currently displayed object diagram.
     * @return The list of objects available in the currently displayed object diagram.
     */
    public List<MObjectState> getObjectsFromModel(UseSystemApi api) {
        return USEUtils.getObjectsOfClass(api, retrievedClass);
    }

    public abstract void saveObjectsToDataLake(UseSystemApi api, Jedis jedis) throws UseApiException;

    /**
     * This method is equivalent to the redis command <i>ZADD DT_sensorKey_LIST score registryKey</i>
     *
     * @param sensorKey   Sensor identifier.
     * @param score       Value of the sensor readings.
     * @param registryKey Snapshot Id
     * @param jedis       An instance of the Jedis client to access the data lake.
     */
    protected void addSearchRegister(String sensorKey, double score, String registryKey, Jedis jedis, String executionId) {
        jedis.zadd(executionId + ":" + sensorKey.toUpperCase() + "_LIST", score, registryKey);
    }

    /**
     * Retrieves an attribute with the name <i>attributeName</i> from an USE object state.
     *
     * @param objstate      State of the USE object.
     * @param attributeName Name of the attribute whose value is retrieved.
     * @return The corresponding attribute value, or null if the attribute is not found.
     */
    protected String getAttributeAsString(MObjectState objstate, String attributeName) {
        try {
            return objstate.attributeValue(attributeName).toString();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Auxiliary method to store the object in the database, extracted from the diagram.
     *
     * @param jedis    An instance of the Jedis client to access the data lake.
     * @param snapshot The object to store.
     */
    protected void saveOneObject(Jedis jedis, MObjectState snapshot) {
        Map<String, String> armValues = new HashMap<>();

        // Generate the snapshot identifier
        String snapshotId = generateOutputObjectId(snapshot);
        armValues.put(SNAPSHOT_ID, snapshotId);

        // Get execution ID
        String executionId = snapshotId.substring(0, snapshotId.lastIndexOf(":"));

        DTLogger.info(getChannel(), "---");
        for (String attr : attributeSpecification.attributeNames()) {

            AttributeType type = attributeSpecification.typeOf(attr);
            int multiplicity = attributeSpecification.multiplicityOf(attr);

            String attributeValue = getAttributeAsString(snapshot, attr);
            if (attributeValue != null) {
                DTLogger.info(getChannel(), attr + ": " + attributeValue);
                if (multiplicity > 1) {
                    // A sequence of values
                    String[] values = extractValuesFromCollection(attributeValue);
                    if (values.length == multiplicity) {
                        for (int i = 1; i <= multiplicity; i++) {
                            armValues.put(attr + "_" + i, values[i - 1]);
                        }
                    } else {
                        DTLogger.warn(getChannel(), "Attribute " + attr + " has " + values.length
                                + " value(s), but we need " + multiplicity);
                    }
                } else {
                    // A single value
                    armValues.put(attr, type.toRedisString(attributeValue));
                    switch (type) {
                        case NUMBER:
                        case BOOLEAN:
                            addSearchRegister(attr, type.getSearchRegisterScore(attributeValue),
                                    snapshotId, jedis, executionId);
                    }
                }
            } else {
                DTLogger.warn(getChannel(), "Attribute " + attr + " not found in class " + retrievedClass);
            }
        }

        // Save the snapshot
        DTLogger.info(getChannel(), "Saved snapshot: " + snapshotId);
        DTLogger.info(getChannel(), "---");

        jedis.hset(snapshotId, armValues);
        jedis.zadd(processedObjectsSetIdentifier, 0, snapshotId);
    }

    protected String generateOutputObjectId(MObjectState objstate) {
        return String.format(
                "DTOutputSnapshot:%s:%s:%s",
                StringUtils.removeQuotes(getAttributeAsString(objstate, "twinId")),
                StringUtils.removeQuotes(getAttributeAsString(objstate, "executionId")),
                StringUtils.removeQuotes(getAttributeAsString(objstate, "timestamp")));
    }

    private String[] extractValuesFromCollection(String value) {
        value = value.replaceAll("(?:Set|Bag|OrderedSet|Sequence)\\{(.*)}", "$1");
        return value.split(",");
    }

}
