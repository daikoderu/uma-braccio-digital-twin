package services;

import digital.twin.OutputManager;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.tzi.use.uml.sys.MObjectState;
import plugin.DriverConfig;
import utils.DTLogger;

import java.util.List;

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
	 * @param driver The driver to use to connect to the Neo4j data lake
	 */
	public OutService(String channel, OutputManager outputManager, Driver driver) {
		super(channel, DriverConfig.SLEEP_TIME_MS, driver);
		this.output = outputManager;
	}
	
	/**
	 * Checks periodically if there are new output objects in the currently displayed object diagram on USE.
	 */
	public void action() {
		try (Session session = driver.session()) {
			List<MObjectState> useObjects = output.getUnprocessedModelObjects();
			if (!useObjects.isEmpty()) {
				DTLogger.info(getChannel(), "New Information");
				output.saveObjectsToDataLake(session, useObjects);
			}
		} catch (Exception ex) {
			DTLogger.error("An error ocurred: ", ex);
		}
    }

}
