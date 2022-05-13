package pubsub;

import digital.twin.DTUseFacade;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

public class TimePubService extends PubService {

    public static final String DL_NOW = "now";
    public static final int RESOLUTION_MS = 100;

    private final JedisPool jedisPool;
    private final DTUseFacade useApi;

    /**
     * Default constructor.
     *
     * @param channel The channel to send the event to
     * @param jedisPool The Jedis client pool connected to the data lake
     */
    public TimePubService(String channel, JedisPool jedisPool, DTUseFacade useApi) {
        super(channel, RESOLUTION_MS);
        this.jedisPool = jedisPool;
        this.useApi = useApi;
    }

    @Override
    protected void action() {
        try (Jedis jedis = jedisPool.getResource()) {
            int dlTime = getRedisTimestamp(jedis);
            int useTime = useApi.getCurrentTime();
            if (dlTime >= useTime + RESOLUTION_MS) {
                jedis.publish(getChannel(), "Tick received");
                DTLogger.info(this, "Tick received");
            }
        } catch (Exception ex) {
            DTLogger.error("An error ocurred:", ex);
        }
    }

    static int getRedisTimestamp(Jedis jedis) {
        if (jedis.exists(DL_NOW)) {
            return Integer.parseInt(jedis.get(DL_NOW));
        } else {
            return 0;
        }
    }

}
