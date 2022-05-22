package pubsub;

import digital.twin.DTUseFacade;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

public class TimePubService extends PubService {

    public static final String PT_NOW = "PTnow";
    public static final String DT_NOW = "DTnow";
    public static final int RESOLUTION_MS = 100;

    private final JedisPool jedisPool;
    private final DTUseFacade useApi;

    /**
     * Default constructor.
     *
     * @param channel The channel to send the event to
     * @param jedisPool The Jedis client pool connected to the data lake
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     */
    public TimePubService(String channel, JedisPool jedisPool, DTUseFacade useApi) {
        super(channel, RESOLUTION_MS);
        this.jedisPool = jedisPool;
        this.useApi = useApi;
    }

    @Override
    protected void action() {
        try (Jedis jedis = jedisPool.getResource()) {
            int dlTime = getDTTimestampInDataLake(jedis);
            int useTime = useApi.getCurrentTime();
            if (dlTime >= useTime + RESOLUTION_MS) {
                jedis.publish(getChannel(), "Tick received");
            }
        } catch (Exception ex) {
            DTLogger.error("An error ocurred:", ex);
        }
    }

    static int getDTTimestampInDataLake(Jedis jedis) {
        if (jedis.exists(DT_NOW)) {
            return Integer.parseInt(jedis.get(DT_NOW));
        } else {
            return 0;
        }
    }

}
