package digital.twin;

import org.tzi.use.uml.sys.MObjectState;
import pubsub.DTPubSub;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * OutputManager that retrieves all Command instances and serializes them for storage in the data lake.
 */
public class CommandResultManager extends OutputManager {

    public CommandResultManager(DTUseFacade useApi) {
        super(useApi, DTPubSub.COMMAND_OUT_CHANNEL, "CommandResult", "DTCommandResult");
        attributeSpecification.set("twinId", AttributeType.STRING);
        attributeSpecification.set("executionId", AttributeType.STRING);
        attributeSpecification.set("commandId", AttributeType.INTEGER);
        attributeSpecification.set("commandName", AttributeType.STRING);
        attributeSpecification.set("commandArguments", AttributeType.STRING);
        attributeSpecification.set("commandTimestamp", AttributeType.INTEGER);
        attributeSpecification.set("return", AttributeType.STRING);
    }

    protected String getObjectId(MObjectState objstate) {
        String twinId = useApi.getStringAttribute(objstate, "twinId");
        String executionId = useApi.getStringAttribute(objstate, "executionId");
        int commandId = useApi.getIntegerAttribute(objstate, "commandId");
        return twinId + ":" + executionId + ":" + commandId;
    }

    protected void addObjectQueryRegisters(
            Jedis jedis, String objectTypeAndId, Map<String, String> values) { }

    protected void addAttributeQueryRegisters(
            Jedis jedis, String objectTypeAndId, String attributeName,
            AttributeType type, String attributeValue) { }

}
