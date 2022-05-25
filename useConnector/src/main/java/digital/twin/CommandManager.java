package digital.twin;

import pubsub.DTPubSub;

import java.util.Map;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * InputManager that retrieves all Commands in the data lake and converts them to USE objects.
 */
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
        if ("moveto".equals(commandName)) {
            return "MoveToPositionCommand";
        } else if ("freeze".equals(commandName)) {
            return "FreezeCommand";
        }
        throw new RuntimeException("Invalid command: " + commandName);
    }

}
