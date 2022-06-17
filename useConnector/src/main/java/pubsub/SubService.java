package pubsub;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that subscribes to events from a PubService.
 */
public class SubService implements Runnable {
	
	private final DTPubSub pubsub;
	private final JedisPool jedisPool;
	private final String subscribedChannel;
	
	/**
	 * Default constructor
	 * 
	 * @param pubsub The listener to be used to handle events
	 * @param jedisPool	Jedis client pool, connected to the Data Lake
	 * @param subscribedChannel	Channel to subscribe to
	 */
	public SubService(DTPubSub pubsub, JedisPool jedisPool, String subscribedChannel) {
		this.pubsub = pubsub;
		this.jedisPool = jedisPool;
		this.subscribedChannel = subscribedChannel;
	}

	/**
	 * Subscribes to the publisher channel specified in the constructor.
	 */
	public void run() {
		DTLogger.info(subscribedChannel, "Subscribing to channel");
        try (Jedis jedisSubscriber = jedisPool.getResource()) {
        	jedisSubscriber.subscribe(pubsub, subscribedChannel);
        	DTLogger.info(subscribedChannel, "Subscription ended");
        } catch (Exception ex) {
        	DTLogger.error(subscribedChannel, "An error ocurred:", ex);
        }    
    }

}
