package digital.twin;

import digital.twin.attributes.AttributeType;
import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.sys.MObjectState;
import pubsub.DTPubSub;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 *
 * @author Paula Muñoz - University of Málaga
 *
 */
public class CommandsManager extends OutputManager {

    public CommandsManager() {
        super(DTPubSub.COMMAND_OUT_CHANNEL, "Command", "DTCommand");
        attributeSpecification.set("action", AttributeType.STRING);
        attributeSpecification.set("arguments", AttributeType.STRING);
    }

    /**
     * Saves all the Command objects in the currently displayed object diagram in the data lake.
     * @param api USE system API instance to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @throws UseApiException In case of any error related to the USE API
     */
    public void saveObjectsToDataLake(UseSystemApi api, Jedis jedis) throws UseApiException {
        List<MObjectState> unprocessedCommands = getObjectsFromModel(api);
        for (MObjectState command : unprocessedCommands) {
            saveOneObject(jedis, command);
            api.deleteObjectEx(command.object());
        }
    }

}
