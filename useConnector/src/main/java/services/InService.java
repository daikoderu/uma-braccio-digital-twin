package services;

import digital.twin.InputManager;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import plugin.DriverConfig;
import utils.DTLogger;

import java.util.List;

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
    public InService(String channel, InputManager inputManager, Driver driver) {
        super(channel, DriverConfig.SLEEP_TIME_MS, driver);
        this.input = inputManager;
    }

    /**
     * Checks periodically if there are new input objects in the Data Lake.
     */
    public void action() {
        try (Session session = driver.session()) {
            List<Record> dlObjects = input.getUnprocessedDLObjects(session);
            if (!dlObjects.isEmpty()) {
                DTLogger.info(getChannel(), "New Information");
                input.saveObjectsToUseModel(session, dlObjects);
            }
        } catch (Exception ex) {
            DTLogger.error("An error ocurred: ", ex);
        }
    }

}
