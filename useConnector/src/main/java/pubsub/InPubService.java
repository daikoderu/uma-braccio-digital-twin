package pubsub;

import digital.twin.InputManager;
import plugin.DriverConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events whenever new information appears in the
 * Data Lake.
 */
public class InPubService extends PubService {

    private final JedisPool jedisPool;
    private final InputManager input;

    /**
     * Default constructor.
     * @param channel The channel to send the event to
     * @param jedisPool The Jedis client pool connected to the data lake
     * @param inputManager Manager to use to check for Data Lake objects.
     */
    public InPubService(String channel, JedisPool jedisPool, InputManager inputManager) {
        super(channel, DriverConfig.SLEEP_TIME_MS);
        this.jedisPool = jedisPool;
        this.input = inputManager;
    }

    /**
     * Checks periodically if there are new input objects in the Data Lake.
     */
    public void action() {
        try (Jedis jedis = jedisPool.getResource()) {
            if (!input.getUnprocessedDLObjects(jedis).isEmpty()) {
                jedis.publish(getChannel(), "New Information");
                DTLogger.info(this, "New Information");
            }
        } catch (Exception ex) {
            DTLogger.error("An error ocurred:", ex);
        }
    }

}
