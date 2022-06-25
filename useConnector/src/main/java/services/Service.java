package services;

import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events.
 */
public abstract class Service implements Runnable {

	public static final String DT_OUT_CHANNEL = "DTOutChannel";
	public static final String COMMAND_OUT_CHANNEL = "CommandOutChannel";
	public static final String COMMAND_IN_CHANNEL = "CommandInChannel";
	public static final String TIME_CHANNEL = "TimeChannel";

	private final String channel;
	private final long sleepTime;
	private boolean running;
	private boolean finished;
	
	public Service(String channel, long sleepTime) {
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
		DTLogger.info(channel, "Service stopped");
	}

	public void stop() {
		running = false;
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
