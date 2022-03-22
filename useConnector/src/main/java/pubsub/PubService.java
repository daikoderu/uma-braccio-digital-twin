package pubsub;

/**
 *
 * @author Paula Muñoz - University of Málaga
 *
 */
public abstract class PubService implements Runnable {

	private final String channel;
	
	public PubService(String channel) {
		this.channel = channel;
	}

	public String getChannel() {
		return channel;
	}
	
	public abstract void stop();

}
