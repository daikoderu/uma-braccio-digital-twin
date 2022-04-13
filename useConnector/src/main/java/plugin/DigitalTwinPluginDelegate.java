package plugin;

import digital.twin.CommandsManager;
import digital.twin.OutputSnapshotsManager;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;
import org.tzi.use.uml.sys.MObjectState;
import pubsub.DTPubSub;
import pubsub.OutPubService;
import pubsub.SubService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import utils.DTLogger;
import utils.UseFacade;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Plugin's main class.
 */
public class DigitalTwinPluginDelegate implements IPluginActionDelegate {

    private static final int NUM_EXECUTOR_POOL_THREADS = 3;
    private static final String REDIS_HOSTNAME = "localhost";
    private static final int SLEEP_TIME_MS = 5000;

    private static final String ROBOT_CLASSNAME = "BraccioRobot";
    private static final String EXECUTION_ID_ATTRIBUTE = "executionId";

    private JedisPool jedisPool;
    private ExecutorService executor;
    private boolean connectionIsActive;
    private OutPubService outPublisher;
    private OutPubService commandOutPublisher;
    private UseFacade useApi;

    /**
     * Default constructor
     */
    public DigitalTwinPluginDelegate() {
        ensureThreadPool();
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
        jedisPool = new JedisPool(new JedisPoolConfig(), REDIS_HOSTNAME);
        if (checkConnectionWithDatabase()) {

            // Initialize USE model
            initializeModel();

            // Create publishing service
            outPublisher = new OutPubService(DTPubSub.DT_OUT_CHANNEL, jedisPool,
                    SLEEP_TIME_MS, new OutputSnapshotsManager(useApi));
            commandOutPublisher = new OutPubService(DTPubSub.COMMAND_OUT_CHANNEL, jedisPool,
                    SLEEP_TIME_MS, new CommandsManager(useApi));
            ensureThreadPool();
            executor.submit(outPublisher);
            executor.submit(commandOutPublisher);

            // Create subscribing threads
            Thread outChannelThread = new Thread(
                    new SubService(useApi, jedisPool, DTPubSub.DT_OUT_CHANNEL),
                    DTPubSub.DT_OUT_CHANNEL + " subscriber thread");
            Thread commandOutChannelThread = new Thread(
                    new SubService(useApi, jedisPool, DTPubSub.COMMAND_OUT_CHANNEL),
                    DTPubSub.COMMAND_OUT_CHANNEL + " subscriber thread");
            outChannelThread.start();
            commandOutChannelThread.start();

            connectionIsActive = true;
        }
    }

    /**
     * Stops the connection between USE and the data lake.
     */
    private void disconnect() {
        outPublisher.stop();
        commandOutPublisher.stop();
        connectionIsActive = false;
        DTLogger.info("Connection ended successfully");
    }

    /**
     * Sets the USE API instance to use.
     * @param pluginAction A reference to the currently running USE instance.
     */
    private void setApi(IPluginAction pluginAction) {
        UseSystemApi api = UseSystemApi.create(pluginAction.getSession());
        useApi = new UseFacade(api);
    }

    /**
     * Checks that the connection with the Data Lake works properly.
     * Prints out "Connection successful" if Java successfully connects to the Redis server.
     * @return true if connection is successful, false otherwise.
     */
    private boolean checkConnectionWithDatabase() {
        try {
            Jedis jedis = jedisPool.getResource();
            DTLogger.info("Connection successful");
            DTLogger.info("The server is running: " + jedis.ping());
            jedisPool.returnResource(jedis);
            return true;
        } catch (Exception ex) {
            DTLogger.error("An error ocurred:");
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Creates a thread pool if it has not been created or is not active
     */
    private void ensureThreadPool() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(NUM_EXECUTOR_POOL_THREADS);
        }
    }

    /**
     * Initializes the USE model.
     */
    private void initializeModel() {
        String posixTime = System.currentTimeMillis() + "";

        // Initialize execution IDs of all robots
        for (MObjectState clock : useApi.getObjectsOfClass(ROBOT_CLASSNAME)) {
            useApi.setAttribute(clock, EXECUTION_ID_ATTRIBUTE, posixTime);
        }
    }

}