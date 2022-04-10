package pubsub;

import digital.twin.OutputManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events whenever new output snapshot or command objects appear.
 */
public class OutPubService extends PubService {

	private final JedisPool jedisPool;
	private final int sleepTime;
	private boolean running;
	private final OutputManager output;
	
	/**
	 * Default constructor.
	 * @param channel The channel to send the event to
	 * @param jedisPool The Jedis client pool connected to the data lake
	 * @param sleepTime Milliseconds between each check in the database
	 * @param outputManager Manager to use to check for instances.
	 */
	public OutPubService(String channel, JedisPool jedisPool, int sleepTime, OutputManager outputManager) {
		super(channel);
		this.jedisPool = jedisPool;
		this.sleepTime = sleepTime;
		this.output = outputManager;
		running = true;
	}
	
	/**
	 * Checks periodically if there are new output snapshots in the currently displayed object diagram on USE.
	 */
	public void run() {
        while (running) {
        	// Wait some seconds until it checks again
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            
            // Check for new snapshots
            try (Jedis jedisTemporalConnection = jedisPool.getResource()) {
            	if (!output.getUnprocessedModelObjects().isEmpty()) {
            		jedisTemporalConnection.publish(getChannel(), "New Information");
            		DTLogger.info(this, "New Information");
            	}
            } catch (Exception ex) {
               ex.printStackTrace();
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
