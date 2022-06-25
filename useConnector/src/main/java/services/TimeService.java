package services;

import digital.twin.DTUseFacade;
import plugin.DriverConfig;

/**
 * @author Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events whenever the current timestamp
 * of the Digital Twin, according to the Data Lake, is greater than the USE model's timestamp.
 */
public class TimeService extends Service {

    public static final String DT_NOW = "DTnow";

    private final DTUseFacade useApi;

    /**
     * Default constructor.
     * @param channel The channel to send the event to
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     */
    public TimeService(String channel, DTUseFacade useApi) {
        super(channel, DriverConfig.TICK_PERIOD_MS);
        this.useApi = useApi;
    }

    /**
     * Publish an event to make the clock tick if the Data Lake timestamp is greater than
     * the USE model's
     */
    @Override
    protected void action() {
        // TODO
    }

    static int getDTTimestampInDataLake() {
        // TODO
        return 0;
    }

}
