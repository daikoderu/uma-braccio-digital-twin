package pubsub;

import digital.twin.InputManager;
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
    private final long sleepTime;
    private boolean running;
    private final InputManager input;

    /**
     * Default constructor.
     * @param channel The channel to send the event to
     * @param jedisPool The Jedis client pool connected to the data lake
     * @param sleepTime Milliseconds between each check in the database
     * @param inputManager Manager to use to check for Data Lake objects.
     */
    public InPubService(String channel, JedisPool jedisPool, long sleepTime, InputManager inputManager) {
        super(channel);
        this.jedisPool = jedisPool;
        this.sleepTime = sleepTime;
        this.input = inputManager;
        running = true;
    }

    /**
     * Checks periodically if there are new input objects in the Data Lake.
     */
    public void run() {
        while (running) {
            // Wait some time
            busyWait(sleepTime);

            // Check for new snapshots
            try (Jedis jedisTemporalConnection = jedisPool.getResource()) {
                // TODO
            } catch (Exception ex) {
                DTLogger.error("An error ocurred:", ex);
            }
        }
    }

    /**
     * Stops the periodic search for new snapshots.
     */
    public void stop() {
        running = false;
    }

}
