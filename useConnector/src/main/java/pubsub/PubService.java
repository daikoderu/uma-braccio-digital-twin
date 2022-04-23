package pubsub;

import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events.
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

	protected void busyWait(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			DTLogger.info(this, "Wait interrupted");
		}
	}

}
