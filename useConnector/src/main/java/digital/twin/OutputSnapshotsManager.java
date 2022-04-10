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
 * OutputManager that retrieves all OutputSnapshot instances and serializes them for storage in the data lake.
 */
public class OutputSnapshotsManager extends OutputManager {

    private static final int NUMBER_OF_SERVOS = 6;

    /**
     * Default constructor.
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     */
    public OutputSnapshotsManager(UseFacade useApi) {
        super(useApi, DTPubSub.DT_OUT_CHANNEL, "OutputBraccioSnapshot", "DTOutputSnapshot");
        attributeSpecification.set("currentAngles", AttributeType.NUMBER, NUMBER_OF_SERVOS);
        attributeSpecification.set("targetAngles", AttributeType.NUMBER, NUMBER_OF_SERVOS);
        attributeSpecification.set("currentSpeeds", AttributeType.NUMBER, NUMBER_OF_SERVOS);
        attributeSpecification.set("moving", AttributeType.BOOLEAN);
    }

    /**
     * Saves all the OutputBraccioSnapshot objects in the currently displayed object diagram in the data lake.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @throws UseApiException Any error related to the USE API.
     */
    public void saveObjectsToDataLake(Jedis jedis) throws UseApiException {
        List<MObjectState> outputSnapshots = getUnprocessedModelObjects();
        for (MObjectState snapshot : outputSnapshots) {
            saveOneObject(jedis, snapshot);
        }
    }

}
