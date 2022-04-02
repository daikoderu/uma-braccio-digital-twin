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
public class OutputSnapshotsManager extends OutputManager {

    private static final int NUMBER_OF_SERVOS = 6;

    /**
     * Sets the type of the attributes in a HashMap to parse the attributes for the Data Lake.
     * For example, Booleans will turn into 0 or 1; Numbers will be transformed into Floats.
     */
    public OutputSnapshotsManager() {
        super();
        setChannel(DTPubSub.DT_OUT_CHANNEL);
        retrievedClass = "OutputBraccioSnapshot";
        processedObjectsSetIdentifier = "processedSnapsDT";

        attributeSpecification.set("twinId", AttributeType.STRING);
        attributeSpecification.set("timestamp", AttributeType.NUMBER);
        attributeSpecification.set("executionId", AttributeType.NUMBER);
        attributeSpecification.set("currentAngles", AttributeType.NUMBER, NUMBER_OF_SERVOS);
        attributeSpecification.set("targetAngles", AttributeType.NUMBER, NUMBER_OF_SERVOS);
        attributeSpecification.set("currentSpeeds", AttributeType.NUMBER, NUMBER_OF_SERVOS);
        attributeSpecification.set("moving", AttributeType.BOOLEAN);
    }

    /**
     * Saves all the OutputCarSnapshots object in the currently displayed object diagram in the data lake
     * and then removes them from the diagram.
     *
     * @param api   USE system API instance to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @throws UseApiException In case of any error related to the USE API
     */
    public void saveObjectsToDataLake(UseSystemApi api, Jedis jedis) throws UseApiException {
        List<MObjectState> outputSnapshots = getObjectsFromModel(api);
        for (MObjectState snapshot : outputSnapshots) {
            saveOneObject(jedis, snapshot);
            api.deleteObjectEx(snapshot.object());
        }
    }

}
