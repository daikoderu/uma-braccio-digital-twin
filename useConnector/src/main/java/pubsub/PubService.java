package pubsub;

import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events.
 */
public abstract class PubService implements Runnable {

	private final String channel;
	private final long sleepTime;
	private boolean running;
	
	public PubService(String channel, long sleepTime) {
		this.channel = channel;
		this.sleepTime = sleepTime;
		running = true;
	}

	public String getChannel() {
		return channel;
	}

	public void run() {
		while (running) {
			busyWait(sleepTime);
			action();
		}
	}

	public void stop() {
		running = false;
	}

	protected abstract void action();

	protected void busyWait(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			DTLogger.info(this, "Wait interrupted");
		}
	}

}
