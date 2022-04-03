package digital.twin;

import digital.twin.attributes.AttributeType;
import org.tzi.use.api.UseApiException;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.sys.MObjectState;
import pubsub.DTPubSub;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * OutputManager that retrieves all OutputSnapshot instances and serializes them for storage in the data lake.
 */
public class OutputSnapshotsManager extends OutputManager {

    private static final int NUMBER_OF_SERVOS = 6;

    /**
     * Default constructor.
     */
    public OutputSnapshotsManager() {
        super(DTPubSub.DT_OUT_CHANNEL, "OutputBraccioSnapshot", "DTOutputSnapshot");
        attributeSpecification.set("currentAngles", AttributeType.NUMBER, NUMBER_OF_SERVOS);
        attributeSpecification.set("targetAngles", AttributeType.NUMBER, NUMBER_OF_SERVOS);
        attributeSpecification.set("currentSpeeds", AttributeType.NUMBER, NUMBER_OF_SERVOS);
        attributeSpecification.set("moving", AttributeType.BOOLEAN);
    }

    /**
     * Saves all the OutputCarSnapshots object in the currently displayed object diagram in the data lake
     * and then removes them from the diagram.
     *
     * @param api USE system API instance to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @throws UseApiException Any error related to the USE API.
     */
    public void saveObjectsToDataLake(UseSystemApi api, Jedis jedis) throws UseApiException {
        List<MObjectState> outputSnapshots = getObjectsFromModel(api);
        for (MObjectState snapshot : outputSnapshots) {
            saveOneObject(jedis, snapshot);
            api.deleteObjectEx(snapshot.object());
        }
    }

}
