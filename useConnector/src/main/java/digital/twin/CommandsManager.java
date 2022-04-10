package digital.twin;

import digital.twin.attributes.AttributeType;
import org.tzi.use.api.UseApiException;
import org.tzi.use.uml.sys.MObjectState;
import pubsub.DTPubSub;
import redis.clients.jedis.Jedis;
import utils.UseFacade;

import java.util.List;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * OutputManager that retrieves all Command instances and serializes them for storage in the data lake.
 */
public class CommandsManager extends OutputManager {

    public CommandsManager(UseFacade useApi) {
        super(useApi, DTPubSub.COMMAND_OUT_CHANNEL, "Command", "DTCommand");
        attributeSpecification.set("action", AttributeType.STRING);
        attributeSpecification.set("arguments", AttributeType.STRING);
    }

    /**
     * Saves all the Command objects in the currently displayed object diagram in the data lake.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @throws UseApiException In case of any error related to the USE API
     */
    public void saveObjectsToDataLake(Jedis jedis) throws UseApiException {
        List<MObjectState> unprocessedCommands = getUnprocessedModelObjects();
        for (MObjectState command : unprocessedCommands) {
            saveOneObject(jedis, command);
        }
    }

}
