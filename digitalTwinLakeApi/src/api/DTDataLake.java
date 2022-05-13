package api;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class DTDataLake implements Closeable {

    private final Jedis jedis;

    DTDataLake(JedisPool pool) {
        jedis = pool.getResource();
    }

    @Override
    public void close() {
        jedis.close();
    }

    public boolean ping() {
        return jedis.ping().equalsIgnoreCase("PONG");
    }

    public int getTime() {
        if (jedis.exists("now")) {
            return Integer.parseInt(jedis.get("now"));
        } else {
            return 0;
        }
    }
    public void advanceTime(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        jedis.incrBy("now", amount);
    }

    public String getCurrentExecutionId() {
        if (jedis.exists("executionId")) {
            return jedis.get("executionId");
        } else {
            return null;
        }
    }

    public int getCommandCounter() {
        if (jedis.exists("commandCounter")) {
            return Integer.parseInt(jedis.get("commandCounter"));
        } else {
            return 0;
        }
    }

    public void putCommand(String twinId, String command, String[] args) {
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
        jedis.hset("DTCommand:" + objectId, hash);
        jedis.zadd("DTCommand_UNPROCESSED", commandId, "DTCommand:" + objectId);
        jedis.hset("PTCommand:" + objectId, hash);
        jedis.zadd("PTCommand_UNPROCESSED", commandId, "PTCommand:" + objectId);
    }

    public void incrCommandCounter() {
        jedis.incr("commandCounter");
    }

}
