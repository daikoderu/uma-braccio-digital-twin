package pubsub;

import org.tzi.use.api.UseSystemApi;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that subscribes to events from a PubService.
 */
public class SubService implements Runnable {
	
	private final UseSystemApi api;
	private final JedisPool jedisPool;
	private final String subscribedChannel;
	
	/**
	 * Default constructor
	 * 
	 * @param api USE system API instance to interact with the currently displayed object diagram
	 * @param jedisPool	Jedis client pool, connected to the Data Lake
	 * @param subscribedChannel	Channel you want to subscribe to
	 */
	public SubService(UseSystemApi api, JedisPool jedisPool, String subscribedChannel) {
		this.api = api;
		this.jedisPool = jedisPool;
		this.subscribedChannel = subscribedChannel;
	}

	/**
	 * Subscribes to the publisher channel specified in the constructor.
	 */
	public void run() {
		DTLogger.info("Subscribing to " + subscribedChannel);
        try (Jedis jedisSubscriber = jedisPool.getResource(); Jedis jedisCrud = jedisPool.getResource()) {
        	jedisSubscriber.subscribe(new DTPubSub(api, jedisCrud), subscribedChannel);
        } catch (Exception ex) {
        	DTLogger.error("An error ocurred:");
            ex.printStackTrace();
        }    
    }

}
