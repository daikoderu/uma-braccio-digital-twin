package digital.twin;

import org.tzi.use.api.UseApiException;
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
            Jedis jedis, String objectTypeAndId, Map<String, String> values) {
        // Make a snapshot history for each twin and execution ID
        String idWithNoTimestamp = objectTypeAndId.substring(0, objectTypeAndId.lastIndexOf(':'));
        int timestamp = Integer.parseInt(values.get("timestamp"));
        jedis.zadd(idWithNoTimestamp + "_HISTORY", timestamp, objectTypeAndId);
    }

    protected void addAttributeQueryRegisters(
            Jedis jedis, String objectTypeAndId, String attributeName,
            AttributeType type, String attributeValue) { }

    protected void cleanUpModel(MObjectState objstate) throws UseApiException {
        useApi.destroyObject(objstate);
    }

}
