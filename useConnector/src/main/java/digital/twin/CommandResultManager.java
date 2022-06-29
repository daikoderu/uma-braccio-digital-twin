package digital.twin;

import org.neo4j.driver.Transaction;
import org.tzi.use.uml.sys.MObjectState;
import services.Service;

import static org.neo4j.driver.Values.parameters;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * OutputManager that retrieves all Command instances and serializes them for storage in the data lake.
 */
public class CommandResultManager extends OutputManager {

    private static final String NODE_LABEL = "CommandResult";

    public CommandResultManager(DTUseFacade useApi) {
        super(useApi, Service.COMMAND_OUT_CHANNEL, "CommandResult", NODE_LABEL, "RETURNED");
        attributeSpecification.set("return", AttributeType.STRING);
    }

    @Override
    protected String getObjectId(MObjectState objstate) {
        useApi.updateDerivedValues();
        String twinId = useApi.getStringAttribute(objstate, "twinId");
        String executionId = useApi.getStringAttribute(objstate, "executionId");
        int commandId = useApi.getIntegerAttribute(objstate, "commandId");
        return twinId + ":" + executionId + ":" + commandId;
    }

    @Override
    protected void createExtraRelationships(Transaction tx, int nodeId, MObjectState objstate) {
        String twinId = useApi.getStringAttribute(objstate, "twinId");
        String executionId = useApi.getStringAttribute(objstate, "executionId");
        int commandId = useApi.getIntegerAttribute(objstate, "commandId");
        tx.run("MATCH (c:Command)<-[:RECEIVED]-(r:BraccioRobot)-[:RETURNED]->(cr:CommandResult) " +
                        "WHERE r.twinId = $twinId AND r.executionId = $executionId " +
                        "AND c.commandId = $commandId AND id(cr) = $nodeId " +
                        "CREATE (cr)-[:FOR]->(c)",
                parameters(
                        "twinId", twinId,
                        "executionId", executionId,
                        "commandId", commandId,
                        "nodeId", nodeId));
    }

    protected void cleanUpModel(MObjectState objstate) { }

}
