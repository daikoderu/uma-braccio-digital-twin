package digital.twin;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import services.Service;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * InputManager that retrieves all Commands in the data lake and converts them to USE objects.
 */
public class CommandManager extends InputManager {

    public CommandManager(DTUseFacade useApi) {
        super(useApi, Service.COMMAND_IN_CHANNEL, "Command", "RECEIVED");
        attributeSpecification.set("name", AttributeType.STRING);
        attributeSpecification.set("arguments", AttributeType.STRING);
        attributeSpecification.set("commandId", AttributeType.INTEGER);
    }

    @Override
    protected String getTargetClass(Record rec) {
        Node i = rec.get("i").asNode();
        String commandName = i.get("name").asString();
        if ("moveto".equals(commandName)) {
            return "MoveToPositionCommand";
        } else if ("freeze".equals(commandName)) {
            return "FreezeCommand";
        }
        throw new RuntimeException("Invalid command: " + commandName);
    }

    @Override
    protected String getObjectId(Record rec) {
        Node i = rec.get("i").asNode();
        return rec.get("r.twinId") + ":" + rec.get("r.executionId") + ":" + i.get("commandId");
    }

    @Override
    protected String getOrdering() {
        return "i.commandId";
    }

}
