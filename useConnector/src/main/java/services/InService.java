package services;

import digital.twin.InputManager;
import plugin.DriverConfig;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events whenever new information appears in the
 * Data Lake.
 */
public class InService extends Service {

    private final InputManager input;

    /**
     * Default constructor.
     * @param channel The channel to send the event to
     * @param inputManager Manager to use to check for Data Lake objects.
     */
    public InService(String channel, InputManager inputManager) {
        super(channel, DriverConfig.SLEEP_TIME_MS);
        this.input = inputManager;
    }

    /**
     * Checks periodically if there are new input objects in the Data Lake.
     */
    public void action() {
        // TODO
    }

}
