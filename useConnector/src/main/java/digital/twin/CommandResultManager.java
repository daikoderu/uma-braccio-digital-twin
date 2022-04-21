package digital.twin;

import digital.twin.attributes.AttributeType;
import pubsub.DTPubSub;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * OutputManager that retrieves all Command instances and serializes them for storage in the data lake.
 */
public class CommandResultManager extends OutputManager {

    public CommandResultManager(DTUseFacade useApi) {
        super(useApi, DTPubSub.COMMAND_OUT_CHANNEL, "CommandResult", "DTCommandResult");
        attributeSpecification.set("command.name", AttributeType.STRING);
        attributeSpecification.set("command.arguments", AttributeType.STRING);
        attributeSpecification.set("command.timestamp", AttributeType.INTEGER);
        attributeSpecification.set("return", AttributeType.STRING);
    }

}
