package pubsub;

import digital.twin.DTUseFacade;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that subscribes to events from a PubService.
 */
public class SubService implements Runnable {
	
	private final DTUseFacade useApi;
	private final JedisPool jedisPool;
	private final String subscribedChannel;
	
	/**
	 * Default constructor
	 * 
	 * @param useApi USE API facade instance to interact with the currently displayed object diagram.
	 * @param jedisPool	Jedis client pool, connected to the Data Lake
	 * @param subscribedChannel	Channel you want to subscribe to
	 */
	public SubService(DTUseFacade useApi, JedisPool jedisPool, String subscribedChannel) {
		this.useApi = useApi;
		this.jedisPool = jedisPool;
		this.subscribedChannel = subscribedChannel;
	}

	/**
	 * Subscribes to the publisher channel specified in the constructor.
	 */
	public void run() {
		DTLogger.info("Subscribing to " + subscribedChannel);
        try (Jedis jedisSubscriber = jedisPool.getResource(); Jedis jedisCrud = jedisPool.getResource()) {
        	jedisSubscriber.subscribe(new DTPubSub(useApi, jedisCrud), subscribedChannel);
        } catch (Exception ex) {
        	DTLogger.error("An error ocurred:");
            ex.printStackTrace();
        }    
    }

}
