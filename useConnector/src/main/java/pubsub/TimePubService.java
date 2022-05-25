package pubsub;

import digital.twin.DTUseFacade;
import plugin.DriverConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

/**
 * @author Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events whenever the current timestamp
 * of the Digital Twin, according to the Data Lake, is greater than the USE model's timestamp.
 */
public class TimePubService extends PubService {

    public static final String DT_NOW = "DTnow";

    private final JedisPool jedisPool;
    private final DTUseFacade useApi;

    /**
     * Default constructor.
     * @param channel The channel to send the event to
     * @param jedisPool The Jedis client pool connected to the data lake
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     */
    public TimePubService(String channel, JedisPool jedisPool, DTUseFacade useApi) {
        super(channel, DriverConfig.TICK_PERIOD_MS);
        this.jedisPool = jedisPool;
        this.useApi = useApi;
    }

    /**
     * Publish an event to make the clock tick if the Data Lake timestamp is greater than
     * the USE model's
     */
    @Override
    protected void action() {
        try (Jedis jedis = jedisPool.getResource()) {
            int dlTime = getDTTimestampInDataLake(jedis);
            int useTime = useApi.getCurrentTime();
            if (dlTime >= useTime + DriverConfig.TICK_PERIOD_MS) {
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
