package services;

import digital.twin.DTNeo4jUtils;
import digital.twin.DTUseFacade;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import plugin.DriverConfig;
import utils.DTLogger;

/**
 * @author Daniel Pérez - University of Málaga
 * Class for a thread that generates ("publishes") events whenever the current timestamp
 * of the Digital Twin, according to the Data Lake, is greater than the USE model's timestamp.
 */
public class TimeService extends Service {

    private final DTUseFacade useApi;

    /**
     * Default constructor.
     * @param channel The channel to send the event to
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     */
    public TimeService(String channel, DTUseFacade useApi, Driver driver) {
        super(channel, DriverConfig.TICK_PERIOD_MS, driver);
        this.useApi = useApi;
    }

    /**
     * Publish an event to make the clock tick if the Data Lake timestamp is greater than
     * the USE model's
     */
    @Override
    protected void action() {
        try (Session session = driver.session()) {
            int dlTime = session.readTransaction(DTNeo4jUtils::getDTTimestampInDataLake);
            int useTime = useApi.getCurrentTime();
            if (dlTime >= useTime + DriverConfig.TICK_PERIOD_MS) {
                int ticks = (dlTime - useTime) / DriverConfig.TICK_PERIOD_MS;
                useApi.advanceTime(ticks);
            }
        } catch (Exception ex) {
            DTLogger.error("An error ocurred:", ex);
        }
    }

}
