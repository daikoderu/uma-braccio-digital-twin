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
        super(useApi, Service.COMMAND_OUT_CHANNEL, "CommandResult", NODE_LABEL);
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
    protected void createRelationships(Transaction tx, int nodeId, MObjectState objstate) {
        // TODO
        /*
        String twinId = useApi.getStringAttribute(objstate, "twinId");
        String executionId = useApi.getStringAttribute(objstate, "executionId");
        tx.run("MATCH (c:Command), (o:" + NODE_LABEL + ") " +
                        "WHERE c.twinId = $twinId AND c.executionId = $executionId " +
                        "AND c.commandId AND id(o) = $id " +
                        "CREATE (c)-[:RETURNED]->(o)",
                parameters(
                        "twinId", twinId,
                        "executionId", executionId,
                        "id", nodeId));

         */
    }

    protected void cleanUpModel(MObjectState objstate) { }

}
