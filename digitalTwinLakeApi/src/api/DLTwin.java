package api;

import java.util.*;

@SuppressWarnings("unused")
public class DLTwin {

    private final String twinId;
    private final String executionId;
    private final DTDataLake dataLake;

    private DLTwin(DTDataLake dataLake, String twinId, String executionId) {
        this.twinId = twinId;
        this.executionId = executionId;
        this.dataLake = dataLake;
    }
    DLTwin(DTDataLake dataLake, String twinId) {
        this(dataLake, twinId, dataLake.getCurrentExecutionId());
    }

    /**
     * Generates and returns a new DLTwin object to query a twin in a specific executionId.
     * @param executionId The executionId to query in.
     * @return The resulting DLTwin object.
     */
    public DLTwin at(String executionId) {
        return new DLTwin(dataLake, twinId, executionId);
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
        // TODO
        return 0;
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
        // TODO
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
