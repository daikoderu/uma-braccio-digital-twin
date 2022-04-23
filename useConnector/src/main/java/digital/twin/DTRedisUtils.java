package digital.twin;

import redis.clients.jedis.Jedis;

public class DTRedisUtils {

    public static final String DL_NOW = "now";
    public static final String DL_EXECUTION_ID = "executionId";
    public static final String DL_COMMAND_COUNTER = "commandCounter";

    protected Jedis jedis;

    public DTRedisUtils(Jedis jedis) {
        this.jedis = jedis;
    }

    public void updateTimestamp(DTUseFacade useApi) {
        int currentTimestamp = Integer.parseInt(jedis.get(DL_NOW));
        int objectTimestamp = useApi.getCurrentTime();
        jedis.set(DL_NOW, Math.max(currentTimestamp, objectTimestamp) + "");
    }

}
