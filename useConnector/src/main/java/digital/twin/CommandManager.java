package digital.twin;

import digital.twin.attributes.AttributeType;
import pubsub.DTPubSub;

import java.util.Map;

public class CommandManager extends InputManager {

    public CommandManager(DTUseFacade useApi) {
        super(useApi, DTPubSub.COMMAND_IN_CHANNEL, "DTCommand");
        attributeSpecification.set("twinId", AttributeType.STRING);
        attributeSpecification.set("executionId", AttributeType.STRING);
        attributeSpecification.set("name", AttributeType.STRING);
        attributeSpecification.set("arguments", AttributeType.STRING);
        attributeSpecification.set("commandId", AttributeType.INTEGER);
    }

    @Override
    protected String getTargetClass(Map<String, String> hash) {
        String commandName = hash.get("name");
        switch (commandName) {

            case "moveto":
                return "MoveToPositionCommand";

            default:
                throw new RuntimeException("Invalid command: " + commandName);

        }
    }

}
