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
public class OutputSnapshotsManager extends OutputManager {

    /**
     * Sets the type of the attributes in a HashMap to parse the attributes for the Data Lake.
     * For example, Booleans will turn into 0 or 1; Numbers will be transformed into Floats.
     */
    public OutputSnapshotsManager() {
        super();
        setChannel(DTPubSub.DT_OUT_CHANNEL);
        retrievedClass = "OutputBraccioSnapshot";
        identifier = "processedSnapsDT";

        attributes.put("twinId", STRING);
        attributes.put("timestamp", NUMBER);
        attributes.put("executionId", NUMBER);
        for (int i = 1; i <= 6; i++) {
            attributes.put("currentAngle" + i, NUMBER);
            attributes.put("targetAngle" + i, NUMBER);
            attributes.put("currentSpeed" + i, NUMBER);
        }
        attributes.put("isMoving", BOOLEAN);
        attributes.put("processingQueue", BOOLEAN);
    }

    /**
     * Saves all the OutputCarSnapshots object in the currently displayed object diagram in the data lake
     * and then removes them from the diagram.
     *
     * @param api   USE system API instance to interact with the currently displayed object diagram.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @throws UseApiException In case of any error related to the USE API
     */
    public void saveObjects(UseSystemApi api, Jedis jedis) throws UseApiException {
        List<MObjectState> outputSnapshots = this.getObjects(api);
        for (MObjectState snapshot : outputSnapshots) {
            Map<MAttribute, Value> snapshotAttributes = snapshot.attributeValueMap();
            String snapshotId = generateOutputObjectId("DTOutputSnapshot", snapshotAttributes);
            saveAttributes(jedis, snapshotAttributes, snapshotId);
            api.deleteObjectEx(snapshot.object());
        }
    }

}
