package digital.twin;

import org.tzi.use.uml.sys.MObjectState;
import pubsub.DTPubSub;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * OutputManager that retrieves all OutputSnapshot instances and serializes them for storage in the data lake.
 */
public class OutputSnapshotsManager extends OutputManager {

    private static final int NUMBER_OF_SERVOS = 6;

    /**
     * Default constructor.
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     */
    public OutputSnapshotsManager(DTUseFacade useApi) {
        super(useApi, DTPubSub.DT_OUT_CHANNEL, "OutputBraccioSnapshot", "DTOutputSnapshot");
        attributeSpecification.set("twinId", AttributeType.STRING);
        attributeSpecification.set("executionId", AttributeType.STRING);
        attributeSpecification.set("currentAngles", AttributeType.REAL, NUMBER_OF_SERVOS);
        attributeSpecification.set("targetAngles", AttributeType.REAL, NUMBER_OF_SERVOS);
        attributeSpecification.set("currentSpeeds", AttributeType.REAL, NUMBER_OF_SERVOS);
        attributeSpecification.set("moving", AttributeType.BOOLEAN);
    }

    protected String getObjectId(MObjectState objstate) {
        String twinId = useApi.getStringAttribute(objstate, "twinId");
        String executionId = useApi.getStringAttribute(objstate, "executionId");
        Integer timestamp = useApi.getIntegerAttribute(objstate, "timestamp");
        assert twinId != null;
        assert executionId != null;
        assert timestamp != null;
        return twinId + ":" + executionId + ":" + timestamp;
    }

    protected double getObjectScore(MObjectState objstate) {
        return useApi.getIntegerAttribute(objstate, "timestamp");
    }

    protected void addObjectQueryRegisters(
            Jedis jedis, String objectTypeAndId, Map<String, String> values) { }

    protected void addAttributeQueryRegisters(
            Jedis jedis, String objectTypeAndId, String attributeName,
            AttributeType type, String attributeValue) {
        addHistoryRegister(jedis, objectTypeAndId, attributeName, type, attributeValue);
    }

    /**
     * Adds a search register to the database to maintain a list of all snapshots for each twin.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @param objectTypeAndId The ID of the object to generate the search register for.
     * @param attributeName The name of the attribute to save.
     * @param type The type of the attribute to save.
     * @param attributeValue The value to save, as a Redis value.
     */
    private void addHistoryRegister(
            Jedis jedis, String objectTypeAndId, String attributeName,
            AttributeType type, String attributeValue) {
        String idWithNoTimestamp = objectTypeAndId.substring(0, objectTypeAndId.lastIndexOf(':'));
        double score = type.getScore(attributeValue);
        jedis.zadd(idWithNoTimestamp + ":" + attributeName + "_HISTORY", score, objectTypeAndId);
    }

}
