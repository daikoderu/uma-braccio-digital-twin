package digital.twin;

import org.neo4j.driver.Transaction;
import org.tzi.use.api.UseApiException;
import org.tzi.use.uml.sys.MObjectState;
import services.Service;

import static org.neo4j.driver.Values.parameters;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * OutputManager that retrieves all OutputSnapshot instances and serializes them for storage in the data lake.
 */
public class OutputSnapshotsManager extends OutputManager {

    private static final int NUMBER_OF_SERVOS = 6;
    private static final String NODE_LABEL = "OutputSnapshot";

    /**
     * Default constructor.
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     */
    public OutputSnapshotsManager(DTUseFacade useApi) {
        super(useApi, Service.DT_OUT_CHANNEL, "OutputBraccioSnapshot", NODE_LABEL);
        attributeSpecification.set("currentAngles", AttributeType.REAL, NUMBER_OF_SERVOS);
        attributeSpecification.set("targetAngles", AttributeType.REAL, NUMBER_OF_SERVOS);
        attributeSpecification.set("currentSpeeds", AttributeType.REAL, NUMBER_OF_SERVOS);
        attributeSpecification.set("moving", AttributeType.BOOLEAN);
    }

    @Override
    protected String getObjectId(MObjectState objstate) {
        String twinId = useApi.getStringAttribute(objstate, "twinId");
        String executionId = useApi.getStringAttribute(objstate, "executionId");
        Integer timestamp = useApi.getIntegerAttribute(objstate, "timestamp");
        assert twinId != null;
        assert executionId != null;
        assert timestamp != null;
        return twinId + ":" + executionId + ":" + timestamp;
    }

    @Override
    protected void createRelationships(Transaction tx, int nodeId, MObjectState objstate) {
        String twinId = useApi.getStringAttribute(objstate, "twinId");
        String executionId = useApi.getStringAttribute(objstate, "executionId");
        tx.run("MATCH (r:BraccioRobot), (o:" + NODE_LABEL + ") " +
                        "WHERE r.twinId = $twinId AND r.executionId = $executionId " +
                        "AND id(o) = $id " +
                        "CREATE (r)-[:IS_IN_STATE]->(o)",
                parameters(
                        "twinId", twinId,
                        "executionId", executionId,
                        "id", nodeId));
    }

    protected void cleanUpModel(MObjectState objstate) throws UseApiException {
        useApi.destroyObject(objstate);
    }

}
