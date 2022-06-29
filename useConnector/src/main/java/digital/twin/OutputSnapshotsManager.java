package digital.twin;

import org.neo4j.driver.Transaction;
import org.tzi.use.api.UseApiException;
import org.tzi.use.uml.sys.MObjectState;
import services.Service;

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
    public OutputSnapshotsManager(DTUseFacade useApi) {
        super(useApi, Service.DT_OUT_CHANNEL, "OutputBraccioSnapshot",
                "OutputSnapshot", "IS_AT_STATE");
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
    protected void createExtraRelationships(Transaction tx, int nodeId, MObjectState objstate) { }

    protected void cleanUpModel(MObjectState objstate) throws UseApiException {
        useApi.destroyObject(objstate);
    }

}
