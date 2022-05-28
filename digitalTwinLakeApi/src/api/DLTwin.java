package api;

import redis.clients.jedis.Jedis;

import java.util.*;

@SuppressWarnings("unused")
public class DLTwin {

    private final String twinId;
    private final String executionId;
    private final Jedis jedis;
    private final DTDataLake dataLake;

    private DLTwin(Jedis jedis, DTDataLake dataLake, String twinId, String executionId) {
        this.twinId = twinId;
        this.executionId = executionId;
        this.jedis = jedis;
        this.dataLake = dataLake;
    }
    DLTwin(Jedis jedis, DTDataLake dataLake, String twinId) {
        this(jedis, dataLake, twinId, dataLake.getCurrentExecutionId());
    }

    /**
     * Generates and returns a new DLTwin object to query a twin in a specific executionId.
     * @param executionId The executionId to query in.
     * @return The resulting DLTwin object.
     */
    public DLTwin at(String executionId) {
        return new DLTwin(jedis, dataLake, twinId, executionId);
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
        Map<String, String> hash = new HashMap<>();
        incrCommandCounter();
        StringJoiner argJoiner = new StringJoiner(" ", "", "");
        for (String arg : args) {
            argJoiner.add(arg);
        }
        String execId = dataLake.getCurrentExecutionId();
        int commandId = dataLake.getCommandCounter();
        String objectId = twinId + ":" + execId + ":" + commandId;

        hash.put("twinId", twinId);
        hash.put("executionId", dataLake.getCurrentExecutionId());
        hash.put("name", command);
        hash.put("arguments", argJoiner.toString());
        hash.put("commandId", commandId + "");

        if (target.isPhysical) {
            jedis.hset("PTCommand:" + objectId, hash);
            jedis.zadd("PTCommand_UNPROCESSED", commandId, "PTCommand:" + objectId);
        }
        if (target.isDigital) {
            jedis.hset("DTCommand:" + objectId, hash);
            jedis.zadd("DTCommand_UNPROCESSED", commandId, "DTCommand:" + objectId);
        }
        return commandId;
    }

    private void incrCommandCounter() {
        jedis.incr("commandCounter");
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
        String objectId = target.getPrefix() + "OutputSnapshot" + ":"
                + twinId + ":" + executionId + ":" + timestamp;
        return OutputSnapshot.fromHash(jedis.hgetAll(objectId));
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
        String setId = target.getPrefix() + "OutputSnapshot:" + twinId + ":" + executionId + "_HISTORY";
        Set<String> keys = jedis.zrangeByScore(setId, timestampFrom, timestampTo);
        return deserialize(keys);
    }

    /**
     * Returns the latest <var>amount</var> output snapshots from one of the twins
     * @param target The twin whose snapshots to retrieve.
     * @param amount The number of snapshots to retrieve.
     * @return A list of snapshots.
     */
    public List<OutputSnapshot> getLatestOutputSnapshots(TwinTarget target, int amount) {
        target.requireOneTwin();
        String setId = target.getPrefix() + "OutputSnapshot:" + twinId + ":" + executionId + "_HISTORY";
        Set<String> keys = jedis.zrange(setId, -amount, -1);
        return deserialize(keys);
    }

    private List<OutputSnapshot> deserialize(Collection<String> keys) {
        int maxLength = keys.size();
        List<OutputSnapshot> result = new ArrayList<>(maxLength);
        if (maxLength > 0) {
            for (String k : keys) {
                Map<String, String> hash = jedis.hgetAll(k);
                result.add(OutputSnapshot.fromHash(hash));
            }
        }
        return result;
    }

}
