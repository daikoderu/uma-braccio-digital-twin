package pubsub;

import org.tzi.use.api.UseSystemApi;

import digital.twin.OutputManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events whenever new output snapshot or command objects appear.
 */
public class OutPubService extends PubService {
	
	private final UseSystemApi api;
	private final JedisPool jedisPool;
	private final int sleepTime;
	private boolean running;
	private final OutputManager output;
	
	/**
	 * Default constructor.
	 * @param channel The channel to send the event to
	 * @param api USE system API instance to interact with the currently displayed object diagram
	 * @param jedisPool The Jedis client pool connected to the data lake
	 * @param sleepTime Milliseconds between each check in the database
	 */
	public OutPubService(String channel, UseSystemApi api, JedisPool jedisPool, int sleepTime, OutputManager output) {
		super(channel);
		this.api = api;
		this.jedisPool = jedisPool;
		this.sleepTime = sleepTime;
		this.output = output;
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
            	if (!output.getObjectsFromModel(api).isEmpty()) {
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
