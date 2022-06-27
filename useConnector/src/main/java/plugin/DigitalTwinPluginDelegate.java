package plugin;

import digital.twin.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;
import org.tzi.use.uml.sys.MObjectState;
import services.*;
import utils.DTLogger;

import static org.neo4j.driver.Values.parameters;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Plugin's main class.
 */
public class DigitalTwinPluginDelegate implements IPluginActionDelegate {

    private boolean connectionIsActive;
    private OutService outService;
    private OutService commandOutService;
    private InService commandInService;
    private TimeService timeService;
    private Thread outServiceThread;
    private Thread commandOutServiceThread;
    private Thread commandInServiceThread;
    private Thread timeServiceThread;
    private DTUseFacade useApi;
    private Driver driver;

    /**
     * Default constructor
     */
    public DigitalTwinPluginDelegate() {
        connectionIsActive = false;
    }

    /**
     * This is the Action Method called from the Action Proxy. This is called when the
     * user presses the plugin button.
     * @param pluginAction A reference to the currently running USE instance.
     */
    public void performAction(IPluginAction pluginAction) {
        if (!connectionIsActive) {
            connect(pluginAction);
        } else {
            disconnect();
        }
    }

    /**
     * Creates a connection between USE and the data lake.
     * @param pluginAction A reference to the currently running USE instance.
     */
    private void connect(IPluginAction pluginAction) {
        setApi(pluginAction);
        driver = GraphDatabase.driver("bolt://" + DriverConfig.NEO4J_HOSTNAME);
        if (checkConnectionWithDatabase()) {

            // Initialize USE model
            initialize();

            // Create threads
            outService = new OutService(Service.DT_OUT_CHANNEL,
                    new OutputSnapshotsManager(useApi), driver);
            commandOutService = new OutService(Service.COMMAND_OUT_CHANNEL,
                    new CommandResultManager(useApi), driver);
            commandInService = new InService(Service.DT_OUT_CHANNEL,
                    new CommandManager(useApi), driver);
            timeService = new TimeService(Service.COMMAND_OUT_CHANNEL, useApi, driver);

            outServiceThread = new Thread(outService,
                    Service.DT_OUT_CHANNEL + " subscriber thread");
            commandOutServiceThread = new Thread(commandOutService,
                    Service.COMMAND_OUT_CHANNEL + " subscriber thread");
            commandInServiceThread = new Thread(commandInService,
                    Service.COMMAND_IN_CHANNEL + " subscriber thread");
            timeServiceThread = new Thread(timeService,
                    Service.TIME_CHANNEL + " subscriber thread");
            outServiceThread.start();
            //commandOutServiceThread.start();
            //commandInServiceThread.start();
            //timeServiceThread.start();

            connectionIsActive = true;
        }
    }

    /**
     * Stops the connection between USE and the data lake.
     */
    private void disconnect() {
        try {
            DTLogger.info("Disconnecting...");
            outService.stop();
            commandOutService.stop();
            commandInService.stop();
            timeService.stop();
            outServiceThread.join();
            commandOutServiceThread.join();
            commandInServiceThread.join();
            timeServiceThread.join();
            driver.close();
            connectionIsActive = false;
            DTLogger.info("Connection ended successfully");
        } catch (InterruptedException ex) {
            DTLogger.error("Could not end connection successfully", ex);
        }
    }

    /**
     * Sets the USE API instance to use.
     * @param pluginAction A reference to the currently running USE instance.
     */
    private void setApi(IPluginAction pluginAction) {
        UseSystemApi api = UseSystemApi.create(pluginAction.getSession());
        useApi = new DTUseFacade(api);
    }

    /**
     * Checks that the connection with the Data Lake works properly.
     * Prints out "Connection successful" if Java successfully connects to the Redis server.
     * @return true if connection is successful, false otherwise.
     */
    private boolean checkConnectionWithDatabase() {
        try (Session session = driver.session()) {
            DTLogger.info("Connecting to Neo4j Data Lake...");
            session.writeTransaction(tx -> {
                tx.run("CREATE (p:Ping) RETURN p");
                tx.run("MATCH (p:Ping) DELETE p");
                return null;
            });
            DTLogger.info("Connection successful");
            return true;
        } catch (Exception ex) {
            DTLogger.error("Data lake connection error:", ex);
            return false;
        }
    }

    /**
     * Initializes the model and the data lake.
     */
    private void initialize() {
        try (Session session = driver.session()) {
            String posixTime = System.currentTimeMillis() + "";
            session.writeTransaction(tx -> {

                // Create execution node
                tx.run("MATCH (ex:Execution) DETACH DELETE (ex)");
                tx.run("CREATE (ex:Execution) " +
                    "SET ex.executionId = $executionId, ex.commandCounter = 0 " +
                    "RETURN ex",
                    parameters("executionId", posixTime)
                );
                for (MObjectState robot : useApi.getObjectsOfClass("BraccioRobot")) {
                    useApi.setAttribute(robot, "executionId", posixTime);
                    String twinId = useApi.getStringAttribute(robot, "twinId");

                    // Create robot nodes
                    tx.run("CREATE (r:BraccioRobot) " +
                            "SET r.twinId = $twinId, r.executionId = $executionId, r.isPhysical = false " +
                            "RETURN r",
                            parameters("twinId", twinId, "executionId", posixTime));
                }
                return null;
            });
        } catch (Exception ex) {
            DTLogger.error("Error initializing USE model:", ex);
        }
    }

}