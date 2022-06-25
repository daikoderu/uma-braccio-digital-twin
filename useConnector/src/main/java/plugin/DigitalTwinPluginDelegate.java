package plugin;

import digital.twin.*;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;
import services.*;
import utils.DTLogger;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Plugin's main class.
 */
public class DigitalTwinPluginDelegate implements IPluginActionDelegate {

    private static final String DL_EXECUTION_ID = "executionId";
    private static final String DL_COMMAND_COUNTER = "commandCounter";

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
        if (checkConnectionWithDatabase()) {

            // Initialize USE model
            initializeModel();

            // Create threads
            outService = new OutService(Service.DT_OUT_CHANNEL, new OutputSnapshotsManager(useApi));
            commandOutService = new OutService(Service.COMMAND_OUT_CHANNEL, new CommandResultManager(useApi));
            commandInService = new InService(Service.DT_OUT_CHANNEL, new CommandManager(useApi));
            timeService = new TimeService(Service.COMMAND_OUT_CHANNEL, useApi);

            outServiceThread = new Thread(outService,
                    Service.DT_OUT_CHANNEL + " subscriber thread");
            commandOutServiceThread = new Thread(commandOutService,
                    Service.COMMAND_OUT_CHANNEL + " subscriber thread");
            commandInServiceThread = new Thread(commandInService,
                    Service.COMMAND_IN_CHANNEL + " subscriber thread");
            timeServiceThread = new Thread(timeService,
                    Service.TIME_CHANNEL + " subscriber thread");
            outServiceThread.start();
            commandOutServiceThread.start();
            commandInServiceThread.start();
            timeServiceThread.start();

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
        try {
            // TODO
            DTLogger.info("Connection successful");
            return true;
        } catch (Exception ex) {
            DTLogger.error("Data lake connection error:", ex);
            return false;
        }
    }

    /**
     * Initializes the USE model.
     */
    private void initializeModel() {
        // TODO
    }

    private void setExecutionIds() {
        // TODO
    }

}