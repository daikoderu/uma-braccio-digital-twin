package api;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

import java.util.*;

import static org.neo4j.driver.Values.parameters;

@SuppressWarnings("unused")
public class DLTwin {

    private final String twinId;
    private final String executionId;
    private final DTDataLake dataLake;
    private final Session session;

    private DLTwin(DTDataLake dataLake, Session session, String twinId, String executionId) {
        this.twinId = twinId;
        this.executionId = executionId;
        this.dataLake = dataLake;
        this.session = session;
    }
    DLTwin(DTDataLake dataLake, Session session, String twinId) {
        this(dataLake, session, twinId, dataLake.getCurrentExecutionId());
    }

    /**
     * Generates and returns a new DLTwin object to query a twin in a specific executionId.
     * @param executionId The executionId to query in.
     * @return The resulting DLTwin object.
     */
    public DLTwin at(String executionId) {
        return new DLTwin(dataLake, session, twinId, executionId);
    }

    // Commands
    // --------------------------------------------------------------------------------------------

    /**
     * Puts a command in the Data Lake.
     * @param target Whether to target the Physical Twin, the Digital Twin, or both.
     * @param command The name of the command to send.
     * @param args The arguments to send.
     * @return The ID of the new command.
     */
    public int putCommand(TwinTarget target, String command, String... args) {
        incrCommandCounter();
        int id = dataLake.getCommandCounter();
        StringJoiner argjoiner = new StringJoiner(" ");
        for (String arg : args) {
            argjoiner.add(arg);
        }
        session.writeTransaction(tx -> {
            if (target.isPhysical) {
                putCommandOnce(tx, "MATCH (r:BraccioRobot) WHERE r.twinId = $twinId " +
                        "AND r.executionId = $executionId AND r.isPhysical", command,
                        argjoiner.toString(), id);
            }
            if (target.isDigital) {
                putCommandOnce(tx, "MATCH (r:BraccioRobot) WHERE r.twinId = $twinId " +
                        "AND r.executionId = $executionId AND NOT r.isPhysical", command,
                        argjoiner.toString(), id);
            }
            return null;
        });
        return id;
    }

    public Command getCommand(TwinTarget target, int commandId) {
        target.requireOneTwin();
        // TODO
        return null;
    }

    public boolean commandHasResult(TwinTarget target, int commandId) {
        // TODO
        return false;
    }

    private void incrCommandCounter() {
        session.writeTransaction(tx -> {
            Result result = tx.run("MATCH (ex:Execution) SET ex.commandCounter = ex.commandCounter + 1");
            return null;
        });
    }

    private void putCommandOnce(Transaction tx, String match, String command, String args, int id) {
        tx.run(match +
                " CREATE (r)-[:RECEIVED]->(c:Command) " +
                "SET c.name = $name, c.arguments = $arguments, c.commandId = $commandId, " +
                "c.isProcessed = false",
                parameters(
                        "twinId", twinId,
                        "executionId", executionId,
                        "commandId", id,
                        "name", command,
                        "arguments", args
        ));
    }

    // Snapshots
    // --------------------------------------------------------------------------------------------

    /**
     * Returns an output snapshot from one of the twins
     * @param target The twin whose snapshots to retrieve.
     * @param timestamp The timestamp of the snapshot.
     * @return The resulting snapshot, or null if it does not exist.
     */
    public OutputSnapshot getOutputSnapshot(TwinTarget target, int timestamp) {
        target.requireOneTwin();
        // TODO
        return null;
    }

    /**
     * Returns a list of output snapshots from one of the twins generated during a time interval.
     * @param target The twin whose snapshots to retrieve.
     * @param timestampFrom The first timestamp to return results from.
     * @param timestampTo The last timestamp to return results from.
     * @return A list of snapshots.
     */
    public List<OutputSnapshot> getOutputSnapshotsInRange(TwinTarget target, int timestampFrom, int timestampTo) {
        target.requireOneTwin();
        // TODO
        return null;
    }

    /**
     * Returns the latest <var>amount</var> output snapshots from one of the twins
     * @param target The twin whose snapshots to retrieve.
     * @param amount The number of snapshots to retrieve.
     * @return A list of snapshots.
     */
    public List<OutputSnapshot> getLatestOutputSnapshots(TwinTarget target, int amount) {
        target.requireOneTwin();
        // TODO
        return null;
    }

    private List<OutputSnapshot> deserialize(Collection<String> keys) {
        // TODO
        return null;
    }

}
