package pubsub;

import digital.twin.OutputManager;
import plugin.DriverConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events whenever new output snapshot or command objects appear.
 */
public class OutPubService extends PubService {

	private final JedisPool jedisPool;
	private final OutputManager output;

	/**
	 * Default constructor.
	 * @param channel The channel to send the event to
	 * @param jedisPool The Jedis client pool connected to the data lake
	 * @param outputManager Manager to use to check for instances.
	 */
	public OutPubService(String channel, JedisPool jedisPool, OutputManager outputManager) {
		super(channel, DriverConfig.SLEEP_TIME_MS);
		this.jedisPool = jedisPool;
		this.output = outputManager;
	}
	
	/**
	 * Checks periodically if there are new output objects in the currently displayed object diagram on USE.
	 */
	public void action() {
		try (Jedis jedis = jedisPool.getResource()) {
			if (!output.getUnprocessedModelObjects().isEmpty()) {
				jedis.publish(getChannel(), "New Information");
				DTLogger.info(this, "New Information");
			}
		} catch (Exception ex) {
		    DTLogger.error("An error ocurred:", ex);
		}
    }

}
