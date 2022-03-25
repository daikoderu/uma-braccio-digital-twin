package digital.twin;

import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;
import redis.clients.jedis.Jedis;
import utils.DTLogger;
import utils.StringUtils;

import java.util.ArrayList;
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
    protected static final String STRING = "str";
    protected static final String NUMBER = "double";
    protected static final String BOOLEAN = "boolean";

    protected final Map<String, String> attributes;
    protected String retrievedClass;
    protected String identifier;
    private String channel;

    public OutputManager() {
        attributes = new HashMap<>();
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
    public List<MObjectState> getObjects(UseSystemApi api) {
        List<MObjectState> snapshots = new ArrayList<>();
        MClass snapshotClass = api.getSystem().model().getClass(retrievedClass);
        for (MObject o : api.getSystem().state().allObjects()) {
            if (o.cls().allSupertypes().contains(snapshotClass)) {
                MObjectState ostate = o.state(api.getSystem().state());
                snapshots.add(ostate);
            }
        }
        return snapshots;
    }

    public abstract void saveObjects(UseSystemApi api, Jedis jedis) throws UseApiException;

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
     * Retrieves an attribute with the name <i>attributeName</i> from a Map of attributes and values.
     *
     * @param attributes    Map with the attributes and its values.
     * @param attributeName Name of the attribute whose value is retrieved.
     * @return The corresponding attribute value
     */
    protected String getAttribute(Map<MAttribute, Value> attributes, String attributeName) {
        for (MAttribute snapshotKey : attributes.keySet()) {
            if (snapshotKey.name().equals(attributeName)) {
                return attributes.get(snapshotKey).toString();
            }
        }
        return null;
    }

    /**
     * Auxiliary method to store the attributes in the database, extracted from the diagram.
     *
     * @param jedis              An instance of the Jedis client to access the data lake.
     * @param snapshotAttributes List with the name of the attributes in the snapshot class
     * @param snapshotId         Snapshot identifier
     */
    protected void saveAttributes(Jedis jedis, Map<MAttribute, Value> snapshotAttributes, String snapshotId) {
        Map<String, String> armValues = new HashMap<>();
        armValues.put(SNAPSHOT_ID, snapshotId);
        String executionId = snapshotId.substring(0, snapshotId.lastIndexOf(":"));
        for (String att : attributes.keySet()) {
            String attributeType = attributes.get(att);
            String attributeValue = getAttribute(snapshotAttributes, att);
            DTLogger.info(getChannel(), att + ": " + attributeValue);
            armValues.put(att, attributeValue);
            switch (attributeType) {
                case NUMBER:
                case BOOLEAN:
                    addSearchRegister(att, getSearchRegisterScore(attributeValue, attributeType),
                            snapshotId, jedis, executionId);
            }
        }
        jedis.hset(snapshotId, armValues);
        jedis.zadd(identifier, 0, snapshotId);
    }

    protected String generateOutputObjectId(String prefix, Map<MAttribute, Value> attributes) {
        return String.format(
                "%s:%s:%s:%s",
                prefix,
                StringUtils.removeQuotes(getAttribute(attributes, "twinId")),
                StringUtils.removeQuotes(getAttribute(attributes, "executionId")),
                StringUtils.removeQuotes(getAttribute(attributes, "timestamp")));
    }

    private static double getSearchRegisterScore(String value, String type) {
        switch (type) {
            case NUMBER:
                return Double.parseDouble(value.replace("'", ""));

            case BOOLEAN:
                return Boolean.parseBoolean(value) ? 1 : 0;

            default:
                return 0;
        }
    }

}
