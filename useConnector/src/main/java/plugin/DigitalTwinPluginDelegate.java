package plugin;

import digital.twin.CommandsManager;
import digital.twin.OutputSnapshotsManager;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.ocl.value.StringValue;
import org.tzi.use.uml.sys.MObjectState;
import pubsub.DTPubSub;
import pubsub.OutPubService;
import pubsub.SubService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import utils.DTLogger;
import utils.USEUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Paula Muñoz - University of Málaga
 *
 * Plugin's main class
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

    /**
     * Default constructor
     */
    public DigitalTwinPluginDelegate() {
        ensureThreadPool();
        connectionIsActive = false;
    }

    /**
     * This is the Action Method called from the Action Proxy
     *
     * @param pluginAction This is the reference to the current USE running instance.
     */
    public void performAction(IPluginAction pluginAction) {
        if (!connectionIsActive) {
            connect(pluginAction);
        } else {
            disconnect();
        }
    }

    private void connect(IPluginAction pluginAction) {
        UseSystemApi api = UseSystemApi.create(pluginAction.getSession());
        jedisPool = new JedisPool(new JedisPoolConfig(), REDIS_HOSTNAME);

        if (checkConnectionWithDatabase()) {

            // Initialize USE model
            initializeModel(api);

            // Create publishing service
            outPublisher = new OutPubService(DTPubSub.DT_OUT_CHANNEL, api, jedisPool,
                    SLEEP_TIME_MS, new OutputSnapshotsManager());
            commandOutPublisher = new OutPubService(DTPubSub.COMMAND_OUT_CHANNEL, api, jedisPool,
                    SLEEP_TIME_MS, new CommandsManager());
            ensureThreadPool();
            executor.submit(outPublisher);
            executor.submit(commandOutPublisher);

            // Create subscribing threads
            Thread outChannelThread = new Thread(
                    new SubService(api, jedisPool, DTPubSub.DT_OUT_CHANNEL),
                    "subscriber " + DTPubSub.DT_OUT_CHANNEL + " thread");
            Thread commandOutChannelThread = new Thread(
                    new SubService(api, jedisPool, DTPubSub.COMMAND_OUT_CHANNEL),
                    "subscriber " + DTPubSub.COMMAND_OUT_CHANNEL + " thread");
            outChannelThread.start();
            commandOutChannelThread.start();

            connectionIsActive = true;
        }
    }

    private void disconnect() {
        outPublisher.stop();
        commandOutPublisher.stop();
        connectionIsActive = false;
        DTLogger.info("Connection ended successfully");
    }

    /**
     * Checks that the connection with the Data Lake works properly.
     * Prints out "Connection Successful" if Java successfully connects to the Redis server.
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

    private void initializeModel(UseSystemApi api) {
        long posixTime = System.currentTimeMillis();
        StringValue stringValue = new StringValue(posixTime + "");

        // Initialize execution IDs of all robots
        MAttribute execIdAttr = USEUtils.getAttribute(api, ROBOT_CLASSNAME, EXECUTION_ID_ATTRIBUTE);
        for (MObjectState clock : USEUtils.getObjectsOfClass(api, ROBOT_CLASSNAME)) {
            clock.setAttributeValue(execIdAttr, stringValue);
        }
    }

}