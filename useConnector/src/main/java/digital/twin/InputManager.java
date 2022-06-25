package digital.twin;

import org.tzi.use.uml.sys.MObjectState;
import utils.DTLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class that retrieves all objects from a data lake and converts them to USE objects.
 */
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
    public Set<String> getUnprocessedDLObjects() {
        // TODO
        return null;
    }

    /**
     * Saves all the objects in the Data Lake to the USE model.
     */
    public void saveObjectsToUseModel() {
        Set<String> unprocessedCommands = getUnprocessedDLObjects();
        for (String key : unprocessedCommands) {
            saveOneObject(key);
        }
    }

    /**
     * Returns the name of the class a Redis hash should be converted to.
     * @param hash The Redis hash to convert.
     * @return The name of the class.
     */
    protected abstract String getTargetClass(Map<String, String> hash);

    /**
     * Auxiliary method to store the object in the USE model, extracted from the Data Lake.
     * @param key The key of the object to store.
     */
    private synchronized void saveOneObject(String key) {
        Map<String, String> hash = null; // jedis.hgetAll(key);
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

            // Save timestamp
            useApi.setAttribute(objstate, TIMESTAMP, useApi.getCurrentTime());

            DTLogger.info(getChannel(), "Saved input object: " + key);
            useApi.updateDerivedValues();
        } catch (Exception ex) {
            DTLogger.error(getChannel(), "Could not create object: " + ex.getMessage());
        }

        // TODO Move object from the "UNPROCESSED" queue to the "PROCESSED" queue.

        // TODO Set whenProcessed to indicate when this instance has been saved to the USE model.
    }

    private int getNumberOfValues(Map<String, String> hash, String attribute) {
        int result = 0;
        while (hash.containsKey(attribute + "_" + (result + 1))) {
            result++;
        }
        return result;
    }

}
