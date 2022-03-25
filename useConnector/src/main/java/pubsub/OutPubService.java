package pubsub;

import org.tzi.use.api.UseSystemApi;

import digital.twin.OutputManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

/**
 *
 * @author Paula Muñoz - University of Málaga
 *
 */
public class OutPubService extends PubService {
	
	private final UseSystemApi api;
	private final JedisPool jedisPool;
	private final int sleepTime;
	private boolean running;
	private final OutputManager output;
	
	/**
	 * Default constructor
	 * 
	 * @param api			USE system API instance to interact with the currently displayed object diagram.
	 * @param jedisPool		Jedis client pool, connected to the Data Lake
	 * @param sleepTime		Milliseconds between each check in the database.
	 */
	public OutPubService(String channel, UseSystemApi api, JedisPool jedisPool, int sleepTime, OutputManager output) {
		super(channel);
		this.api = api;
		this.jedisPool = jedisPool;
		this.sleepTime = sleepTime;
		this.running = true;
		this.output = output;
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
            Jedis jedisTemporalConnection = jedisPool.getResource();
            try {
            	if (!output.getObjectsFromModel(api).isEmpty()) {
            		jedisTemporalConnection.publish(getChannel(), "New Information");
            		DTLogger.info(this, "New Information");
            	}
            } catch (Exception ex) {
               ex.printStackTrace();
            } finally {
               jedisPool.returnResource(jedisTemporalConnection);
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
