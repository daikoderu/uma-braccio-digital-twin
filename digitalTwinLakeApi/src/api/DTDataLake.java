package api;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.Closeable;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author Daniel Pérez - University of Málaga
 * API to access the Data Lake.
 */
@SuppressWarnings("unused")
public class DTDataLake implements Closeable {

    private static final String DT_OUTPUT_SNAPSHOT = "DTOutputSnapshot";
    private static final String PT_OUTPUT_SNAPSHOT = "PTOutputSnapshot";

    private final Jedis jedis;

    DTDataLake(JedisPool pool) {
        jedis = pool.getResource();
    }

    @Override
    public void close() {
        jedis.close();
    }

    /**
     * Performs a Redis ping.
     * @return True if the ping was answered with PONG.
     */
    public boolean ping() {
        return jedis.ping().equalsIgnoreCase("PONG");
    }

    /**
     * Gets the current time for the Physical Twin.
     * @return The value of the Physical Twin's clock.
     */
    public int getPTTime() {
        if (jedis.exists("PTnow")) {
            return Integer.parseInt(jedis.get("PTnow"));
        } else {
            return 0;
        }
    }

    /**
     * Gets the current time for the Digital Twin.
     * @return The value of the Digital Twin's clock.
     */
    public int getDTTime() {
        if (jedis.exists("DTnow")) {
            return Integer.parseInt(jedis.get("DTnow"));
        } else {
            return 0;
        }
    }

    /**
     * Advances the Digital Twin's time.
     * @param amount The number of milliseconds to advance.
     */
    public void advanceDTTime(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        jedis.incrBy("DTnow", amount);
    }

    /**
     * Returns the ID of the current execution.
     * @return The ID of the current execution.
     */
    public String getCurrentExecutionId() {
        if (jedis.exists("executionId")) {
            return jedis.get("executionId");
        } else {
            return null;
        }
    }

    /**
     * Returns the current value of the command counter.
     * @return The current value of the command counter.
     */
    public int getCommandCounter() {
        if (jedis.exists("commandCounter")) {
            return Integer.parseInt(jedis.get("commandCounter"));
        } else {
            return 0;
        }
    }

    // Commands
    // --------------------------------------------------------------------------------------------

    /**
     * Puts a command in the Data Lake.
     * @param twinId The ID of the twin the command refers to.
     * @param target Whether to target the Physical Twin, the Digital Twin, or both.
     * @param command The name of the command to send.
     * @param args The arguments to send.
     * @return The ID of the new command.
     */
    public int putCommand(String twinId, CommandTarget target, String command, String[] args) {
        Map<String, String> hash = new HashMap<>();
        incrCommandCounter();
        StringJoiner argJoiner = new StringJoiner(" ", "", "");
        for (String arg : args) {
            argJoiner.add(arg);
        }
        String execId = getCurrentExecutionId();
        int commandId = getCommandCounter();
        String objectId = twinId + ":" + execId + ":" + commandId;

        hash.put("twinId", twinId);
        hash.put("executionId", getCurrentExecutionId());
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

    // Snapshots (Digital Twin)
    // --------------------------------------------------------------------------------------------

    /**
     * Returns an output snapshot from the Digital Twin.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param executionId The ID of the execution to consider.
     * @param timestamp The timestamp of the snapshot.
     * @return The resulting snapshot, or null if it does not exist.
     */
    public OutputSnapshot getDTOutputSnapshot(String twinId, String executionId, int timestamp) {
        String objectId = DT_OUTPUT_SNAPSHOT + ":" + twinId + ":" + executionId + ":" + timestamp;
        return deserialize(objectId);
    }

    /**
     * Returns an output snapshot from the Digital Twin during the current execution.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param timestamp The timestamp of the snapshot.
     * @return The resulting snapshot, or null if it does not exist.
     */
    public OutputSnapshot getDTOutputSnapshot(String twinId, int timestamp) {
        return getDTOutputSnapshot(twinId, getCurrentExecutionId(), timestamp);
    }

    /**
     * Returns a list of output snapshots from the Digital Twin generated during a time interval.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param executionId The ID of the execution to consider.
     * @param timestampFrom The first timestamp to return results from.
     * @param timestampTo The last timestamp to return results from.
     * @return A list of snapshots.
     */
    public List<OutputSnapshot> getDTOutputSnapshotsInRange(
            String twinId, String executionId, int timestampFrom, int timestampTo) {
        Set<String> keys = jedis.zrangeByScore(DT_OUTPUT_SNAPSHOT, timestampFrom, timestampTo);
        return deserialize(keys, (x) -> sameTwinAndExecutionId(x, twinId, executionId), 0, -1);
    }

    /**
     * Returns a list of output snapshots from the Digital Twin generated during a time interval
     * during the current execution.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param timestampFrom The first timestamp to return results from.
     * @param timestampTo The last timestamp to return results from.
     * @return A list of snapshots.
     */
    public List<OutputSnapshot> getDTOutputSnapshotsInRange(
            String twinId, int timestamp, int timestampFrom, int timestampTo) {
        return getDTOutputSnapshotsInRange(twinId, getCurrentExecutionId(), timestampFrom, timestampTo);
    }

    /**
     * Returns a list of the latest <i>amount</i> snapshots taken to the Digital Twin.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param executionId The ID of the execution to consider.
     * @param amount The amount of snapshots to retrieve.
     * @return A list of up to <i>amount</i> snapshots.
     */
    public List<OutputSnapshot> getDTLatestOutputSnapshots(
            String twinId, String executionId, int amount) {
        Set<String> keys = jedis.zrange(DT_OUTPUT_SNAPSHOT, 0, -1);
        return deserialize(keys, (x) -> sameTwinAndExecutionId(x, twinId, executionId), 0, -amount);
    }

    /**
     * Returns a list of the latest <i>amount</i> snapshots taken to the Digital Twin
     * during the current execution.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param amount The amount of snapshots to retrieve.
     * @return A list of up to <i>amount</i> snapshots.
     */
    public List<OutputSnapshot> getDTLatestOutputSnapshots(
            String twinId, int amount) {
        return getDTLatestOutputSnapshots(twinId, getCurrentExecutionId(), amount);
    }

    // Snapshots (Physical Twin)
    // --------------------------------------------------------------------------------------------

    /**
     * Returns an output snapshot from the Physical Twin.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param executionId The ID of the execution to consider.
     * @param timestamp The timestamp of the snapshot.
     * @return The resulting snapshot, or null if it does not exist.
     */
    public OutputSnapshot getPTOutputSnapshot(String twinId, String executionId, int timestamp) {
        String objectId = PT_OUTPUT_SNAPSHOT + ":" + twinId + ":" + executionId + ":" + timestamp;
        return deserialize(objectId);
    }

    /**
     * Returns an output snapshot from the Physical Twin during the current execution.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param timestamp The timestamp of the snapshot.
     * @return The resulting snapshot, or null if it does not exist.
     */
    public OutputSnapshot getPTOutputSnapshot(String twinId, int timestamp) {
        return getPTOutputSnapshot(twinId, getCurrentExecutionId(), timestamp);
    }

    /**
     * Returns a list of output snapshots from the Physical Twin generated during a time interval.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param executionId The ID of the execution to consider.
     * @param timestampFrom The first timestamp to return results from.
     * @param timestampTo The last timestamp to return results from.
     * @return The resulting snapshot, or null if it does not exist.
     */
    public List<OutputSnapshot> getPTOutputSnapshotsInRange(
            String twinId, String executionId, int timestampFrom, int timestampTo) {
        Set<String> keys = jedis.zrangeByScore(PT_OUTPUT_SNAPSHOT, timestampFrom, timestampTo);
        return deserialize(keys, (x) -> sameTwinAndExecutionId(x, twinId, executionId), 0, -1);
    }

    /**
     * Returns a list of output snapshots from the Physical Twin generated during a time interval
     * during the current execution.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param timestampFrom The first timestamp to return results from.
     * @param timestampTo The last timestamp to return results from.
     * @return The resulting snapshot, or null if it does not exist.
     */
    public List<OutputSnapshot> getPTOutputSnapshotsInRange(
            String twinId, int timestamp, int timestampFrom, int timestampTo) {
        return getPTOutputSnapshotsInRange(twinId, getCurrentExecutionId(), timestampFrom, timestampTo);
    }

    /**
     * Returns a list of the latest <i>amount</i> snapshots taken to the Physical Twin.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param executionId The ID of the execution to consider.
     * @param amount The amount of snapshots to retrieve.
     * @return A list of up to <i>amount</i> snapshots.
     */
    public List<OutputSnapshot> getPTLatestOutputSnapshots(
            String twinId, String executionId, int amount) {
        Set<String> keys = jedis.zrange(DT_OUTPUT_SNAPSHOT, 0, -1);
        return deserialize(keys, (x) -> sameTwinAndExecutionId(x, twinId, executionId), 0, -amount);
    }

    /**
     * Returns a list of the latest <i>amount</i> snapshots taken to the Physical Twin
     * during the current execution.
     * @param twinId The ID of the twin whose snapshot to retrieve.
     * @param amount The amount of snapshots to retrieve.
     * @return A list of up to <i>amount</i> snapshots.
     */
    public List<OutputSnapshot> getPTLatestOutputSnapshots(
            String twinId, int amount) {
        return getDTLatestOutputSnapshots(twinId, getCurrentExecutionId(), amount);
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Deserializes a single output snapshot given its key.
     * @param key The key to convert to a OutputSnapshot.
     * @return The resulting snapshot, or null if the snapshot does not exist.
     */
    private OutputSnapshot deserialize(String key) {
        return OutputSnapshot.fromHash(jedis.hgetAll(key));
    }

    /**
     * Deserializes a list of output snapshots given a list of their keys, filtering and slicing the
     * resulting list of snapshots.
     * @param keys The list of keys whose objects to deserialize.
     * @param filter A filter for deserializing objects.
     * @param from The index of the first snapshot to return in the filtered list.
     * @param to The index of the last snapshot to return in the filtered list.
     * @return The resulting list, filtered and sliced.
     */
    @SuppressWarnings("SameParameterValue")
    private List<OutputSnapshot> deserialize(
            Collection<String> keys, Predicate<Map<String, String>> filter, int from, int to) {
        int maxLength = keys.size();
        List<OutputSnapshot> result = new ArrayList<>(maxLength);
        if (maxLength > 0) {
            int currentPos = 0;
            from = tomod(from, maxLength);
            to = tomod(to, maxLength);
            for (String k : keys) {
                Map<String, String> hash = jedis.hgetAll(k);
                if (filter.test(hash)) {
                    if (currentPos >= from && currentPos <= to) {
                        result.add(OutputSnapshot.fromHash(hash));
                    }
                    currentPos++;
                }
            }
        }
        return result;
    }

    /**
     * Converts a number <var>n</var> to its congruent modulo <var>m</var> such that the
     * result is between 0 and <var>m</var> - 1.
     * @param n The number to convert.
     * @param m The modulo to convert to.
     * @return A number between 0 and <var>m</var> - 1.
     */
    private static int tomod(int n, int m) {
        int result = n % m;
        if (n < 0) {
            result += m;
        }
        return result;
    }

    private static boolean sameTwinAndExecutionId(Map<String, String> subject, String twinId, String executionId) {
        return subject.get("twinId").equals(twinId) && subject.get("executionId").equals(executionId);
    }

}
