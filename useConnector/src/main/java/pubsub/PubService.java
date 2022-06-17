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
	private boolean finished;
	
	public PubService(String channel, long sleepTime) {
		this.channel = channel;
		this.sleepTime = sleepTime;
		running = true;
		finished = false;
	}

	public String getChannel() {
		return channel;
	}

	public void run() {
		while (running) {
			busyWait(sleepTime);
			action();
		}
		finished = true;
		DTLogger.info(channel, "PubService stopped");
	}

	public void stop() {
		running = false;
	}

	public void waitUntilFinished() {
		while (!finished) {
			busyWait(50);
		}
	}

	protected abstract void action();

	protected void busyWait(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			DTLogger.error(channel, "Wait interrupted", ex);
		}
	}

}
