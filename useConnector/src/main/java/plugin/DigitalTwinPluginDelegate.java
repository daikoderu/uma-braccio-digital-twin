package plugin;

import digital.twin.*;
import org.tzi.use.api.UseSystemApi;
import org.tzi.use.runtime.gui.IPluginAction;
import org.tzi.use.runtime.gui.IPluginActionDelegate;
import org.tzi.use.uml.sys.MObjectState;
import pubsub.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import utils.DTLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Plugin's main class.
 */
public class DigitalTwinPluginDelegate implements IPluginActionDelegate {

    public static final long SLEEP_TIME_MS = 5000;

    private static final int NUM_EXECUTOR_POOL_THREADS = 10;
    private static final String REDIS_HOSTNAME = "localhost";
    private static final String DL_EXECUTION_ID = "executionId";
    private static final String DL_COMMAND_COUNTER = "commandCounter";

    private JedisPool jedisPool;
    private ExecutorService executor;
    private boolean connectionIsActive;
    private OutPubService outPublisher;
    private OutPubService commandOutPublisher;
    private InPubService commandInPublisher;
    private TimePubService timePublisher;
    private DTUseFacade useApi;

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
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        jedisPool = new JedisPool(poolConfig, REDIS_HOSTNAME);
        if (checkConnectionWithDatabase()) {

            // Initialize USE model
            initializeModel();

            // Create publishing services
            outPublisher = new OutPubService(DTPubSub.DT_OUT_CHANNEL, jedisPool,
                    new OutputSnapshotsManager(useApi));
            commandOutPublisher = new OutPubService(DTPubSub.COMMAND_OUT_CHANNEL, jedisPool,
                    new CommandResultManager(useApi));
            commandInPublisher = new InPubService(DTPubSub.COMMAND_IN_CHANNEL, jedisPool,
                    new CommandManager(useApi));
            timePublisher = new TimePubService(DTPubSub.TIME_CHANNEL, jedisPool, useApi);
            ensureThreadPool();
            executor.submit(outPublisher);
            executor.submit(commandOutPublisher);
            executor.submit(commandInPublisher);
            executor.submit(timePublisher);

            // Create subscribing threads
            Thread outChannelThread = new Thread(
                    new SubService(useApi, jedisPool, DTPubSub.DT_OUT_CHANNEL),
                    DTPubSub.DT_OUT_CHANNEL + " subscriber thread");
            Thread commandOutChannelThread = new Thread(
                    new SubService(useApi, jedisPool, DTPubSub.COMMAND_OUT_CHANNEL),
                    DTPubSub.COMMAND_OUT_CHANNEL + " subscriber thread");
            Thread commandInChannelThread = new Thread(
                    new SubService(useApi, jedisPool, DTPubSub.COMMAND_IN_CHANNEL),
                    DTPubSub.COMMAND_IN_CHANNEL + " subscriber thread");
            Thread timeChannelThread = new Thread(
                    new SubService(useApi, jedisPool, DTPubSub.TIME_CHANNEL),
                    DTPubSub.TIME_CHANNEL + " subscriber thread");
            outChannelThread.start();
            commandOutChannelThread.start();
            commandInChannelThread.start();
            timeChannelThread.start();

            connectionIsActive = true;
        }
    }

    /**
     * Stops the connection between USE and the data lake.
     */
    private void disconnect() {
        outPublisher.stop();
        commandOutPublisher.stop();
        commandInPublisher.stop();
        timePublisher.stop();
        connectionIsActive = false;
        DTLogger.info("Connection ended successfully");
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
        try (Jedis jedis = jedisPool.getResource()) {
            DTLogger.info("Connection successful");
            DTLogger.info("The server is running: " + jedis.ping());
            return true;
        } catch (Exception ex) {
            DTLogger.error("Data lake connection error:", ex);
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
        try (Jedis jedis = jedisPool.getResource()) {
            setExecutionIds(jedis);
            jedis.set(TimePubService.PT_NOW, "0");
            jedis.set(TimePubService.DT_NOW, "0");
            jedis.set(DL_COMMAND_COUNTER, "0");
        } catch (Exception ex) {
            DTLogger.error("Error initializing USE model:", ex);
        }
    }

    private void setExecutionIds(Jedis jedis) {
        String posixTime = System.currentTimeMillis() + "";
        for (MObjectState clock : useApi.getObjectsOfClass("BraccioRobot")) {
            useApi.setAttribute(clock, "executionId", posixTime);
        }
        jedis.set(DL_EXECUTION_ID, posixTime);
    }

}