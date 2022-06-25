package services;

import digital.twin.OutputManager;
import plugin.DriverConfig;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events whenever new output snapshot or command objects appear.
 */
public class OutService extends Service {

	private final OutputManager output;

	/**
	 * Default constructor.
	 * @param channel The channel to send the event to
	 * @param outputManager Manager to use to check for instances.
	 */
	public OutService(String channel, OutputManager outputManager) {
		super(channel, DriverConfig.SLEEP_TIME_MS);
		this.output = outputManager;
	}
	
	/**
	 * Checks periodically if there are new output objects in the currently displayed object diagram on USE.
	 */
	public void action() {
		// TODO
    }

}
