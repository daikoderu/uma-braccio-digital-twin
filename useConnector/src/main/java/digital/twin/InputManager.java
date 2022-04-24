package digital.twin;

import digital.twin.attributes.AttributeSpecification;
import digital.twin.attributes.AttributeType;
import org.tzi.use.uml.sys.MObjectState;
import redis.clients.jedis.Jedis;
import utils.DTLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class InputManager {

    protected static final String TIMESTAMP = "timestamp";
    protected static final String WHEN_PROCESSED = "whenProcessed";

    private static int instanceCounter = 0;

    protected final AttributeSpecification attributeSpecification;
    protected final DTUseFacade useApi;
    private final String channel;
    private final String objectType;

    /**
     * Default constructor.
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     * @param channel The channel this InputManager is created from.
     * @param objectType The type of the Data Lake objects to deserialize.
     */
    public InputManager(DTUseFacade useApi, String channel, String objectType) {
        attributeSpecification = new AttributeSpecification();
        attributeSpecification.set(TIMESTAMP, AttributeType.INTEGER);
        this.useApi = useApi;
        this.channel = channel;
        this.objectType = objectType;
    }

    /**
     * Returns the channel this InputManager is created from.
     * @return The channel this InputManager is associated with.
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Retrieves the objects of type <i>objectType</i> from the Data Lake
     * @return The list of objects available in the Data Lake.
     */
    public Set<String> getUnprocessedDLObjects(Jedis jedis) {
        return jedis.zrange(objectType + "_UNPROCESSED", 0, -1);
    }

    /**
     * Saves all the objects in the Data Lake to the USE model.
     * @param jedis An instance of the Jedis client to access the data lake.
     */
    public void saveObjectsToUseModel(Jedis jedis) {
        Set<String> unprocessedCommands = getUnprocessedDLObjects(jedis);
        DTRedisUtils redisUtils = new DTRedisUtils(jedis);
        for (String key : unprocessedCommands) {
            saveOneObject(jedis, redisUtils, key);
        }
    }

    /**
     * Auxiliary method to store the object in the USE model, extracted from the Data Lake.
     * @param jedis An instance of the Jedis client to access the data lake.
     * @param redisUtils Instance with utility methods to manipulate our data lake.
     * @param key The key of the object to store.
     */
    private void saveOneObject(Jedis jedis, DTRedisUtils redisUtils, String key) {
        Map<String, String> hash = jedis.hgetAll(key);
        try {
            MObjectState objstate = useApi.createObject(
                    getTargetClass(hash), objectType + ++instanceCounter);
            for (String attr : attributeSpecification.attributeNames()) {
                AttributeType type = attributeSpecification.typeOf(attr);
                int multiplicity = attributeSpecification.multiplicityOf(attr);
                if (multiplicity > 1) {
                    int numberOfValues = getNumberOfValues(hash, attr);
                    if (numberOfValues == multiplicity) {
                        List<Object> values = new ArrayList<>();
                        for (int i = 1; i <= multiplicity; i++) {
                            values.add(type.fromRedisStringToObject(hash.get(attr + "_" + i)));
                        }
                        useApi.setAttribute(objstate, attr, values);
                    } else {
                        DTLogger.warn(getChannel(),
                                "Error saving input object " + key + ": "
                                        + "attribute " + attr + " has " + numberOfValues
                                        + " value(s), but we need " + multiplicity);
                    }
                } else {
                    Object value = type.fromRedisStringToObject(hash.get(attr));
                    useApi.setAttribute(objstate, attr, value);
                }
            }
        } catch (Exception ex) {
            DTLogger.error("Could not create object: " + ex.getMessage());
        }

        // Move object from the "UNPROCESSED" queue to the "PROCESSED" queue.
        double score = jedis.zscore(objectType + "_UNPROCESSED", key);
        jedis.zrem(objectType + "_UNPROCESSED", key);
        jedis.zadd(objectType + "_PROCESSED", score, key);
        DTLogger.info(getChannel(), "Saved input object: " + key);

        // Update the Data Lake's timestamp
        redisUtils.updateTimestamp(useApi);

        // Set whenProcessed to indicate when this instance has been saved to the USE model.
        jedis.hset(key, WHEN_PROCESSED, useApi.getCurrentTime() + "");

        // Evaluate derived values
        useApi.updateDerivedValues();

    }

    protected abstract String getTargetClass(Map<String, String> hash);

    private int getNumberOfValues(Map<String, String> hash, String attribute) {
        int result = 0;
        while (hash.containsKey(attribute + "_" + (result + 1))) {
            result++;
        }
        return result;
    }

}
