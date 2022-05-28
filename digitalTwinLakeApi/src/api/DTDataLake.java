package api;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.Closeable;

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

    /**
     * Generates and returns a DLTwin object to perform queries on a specific twin system
     * in the current executionId.
     * @param twinId The ID of the twin to query.
     * @return A DLTwin object to perform queries on the specified twin.
     */
    public DLTwin forTwin(String twinId) {
        return new DLTwin(jedis, this, twinId);
    }

}
