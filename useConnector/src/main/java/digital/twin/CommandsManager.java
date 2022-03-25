package digital.twin;

import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MObjectState;
import pubsub.DTPubSub;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Paula Muñoz - University of Málaga
 *
 */
public class CommandsManager extends OutputManager {

    public CommandsManager() {
        super();
        this.setChannel(DTPubSub.COMMAND_OUT_CHANNEL);
        this.retrievedClass = "Command";
        this.identifier = "commands";

        attributes.put("twinId", STRING);
        attributes.put("timestamp", NUMBER);
        attributes.put("executionId", NUMBER);

        attributes.put("action", STRING);
    }

    /**
     * Saves all the Commands object in the currently displayed object diagram in the data lake.
     *
     * @param api   USE system API instance to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @throws UseApiException In case of any error related to the USE API
     */
    public void saveObjects(UseSystemApi api, Jedis jedis) throws UseApiException {
        List<MObjectState> unprocessedCommands = getObjects(api);
        for (MObjectState command : unprocessedCommands) {
            Map<MAttribute, Value> commandsAttributes = command.attributeValueMap();
            String commandId = generateOutputObjectId("DTCommand", commandsAttributes);
            saveAttributes(jedis, commandsAttributes, commandId);
            api.deleteObjectEx(command.object());
        }
    }

}
