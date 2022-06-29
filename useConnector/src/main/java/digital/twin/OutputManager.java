package digital.twin;

import org.neo4j.driver.*;
import org.tzi.use.api.UseApiException;
import org.tzi.use.uml.sys.MObjectState;
import utils.DTLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.Values.parameters;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class that retrieves all instances of a USE model class and serializes them for storage in the data lake.
 */
public abstract class OutputManager {

    protected static final String IS_PROCESSED = "isProcessed";
    protected static final String WHEN_PROCESSED = "whenProcessed";

    protected final AttributeSpecification attributeSpecification;
    protected final DTUseFacade useApi;
    private final String channel;
    private final String retrievedClass;
    private final String nodeLabel;
    private final String edgeToRobotLabel;

    /**
     * Default constructor. Constructors from subclasses must set the type of the attributes to serialize
     * using the attributeSpecification instance.
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     * @param channel The channel this OutputManager is created from.
     * @param retrievedClass The class whose instances to retrieve and serialize.
     * @param nodeLabel Label to use for the nodes to be created.
     */
    public OutputManager(DTUseFacade useApi, String channel, String retrievedClass, String nodeLabel,
                         String edgeToRobotLabel) {
        attributeSpecification = new AttributeSpecification();
        this.useApi = useApi;
        this.channel = channel;
        this.retrievedClass = retrievedClass;
        this.nodeLabel = nodeLabel;
        this.edgeToRobotLabel = edgeToRobotLabel;
    }

    /**
     * Returns the channel this OutputManager is created from.
     * @return The channel this OutputManager is associated with.
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Retrieves the objects of class <i>retrievedClass</i> from the currently displayed object diagram.
     * @return The list of objects available in the currently displayed object diagram.
     */
    public List<MObjectState> getUnprocessedModelObjects() {
        List<MObjectState> result = useApi.getObjectsOfClass(retrievedClass);
        result.removeIf(obj -> useApi.getBooleanAttribute(obj, IS_PROCESSED));
        return result;
    }

    /**
     * Saves all the objects in the currently displayed object diagram to the data lake.
     * @param session A Neo4j session object.
     */
    public void saveObjectsToDataLake(Session session, List<MObjectState> unprocessedObjects) {
        useApi.updateDerivedValues();
        for (MObjectState objstate : unprocessedObjects) {
            saveOneObject(session, objstate);
        }
    }

    /**
     * Auxiliary method to store the object in the database, extracted from the diagram.
     * @param objstate The object to store.
     */
    private synchronized void saveOneObject(Session session, MObjectState objstate) {
        useApi.updateDerivedValues();

        int timestamp = useApi.getIntegerAttribute(objstate, "timestamp");
        String objectTypeAndId = nodeLabel + ":" + getObjectId(objstate);

        Map<String, Object> attributes = new HashMap<>();

        session.writeTransaction(tx -> {

            // Collect attributes
            for (String attr : attributeSpecification.attributeNames()) {
                AttributeType attrType = attributeSpecification.typeOf(attr);
                int multiplicity = attributeSpecification.multiplicityOf(attr);
                String attrValue = useApi.getAttributeAsString(objstate, attr);
                if (attrValue != null) {
                    if (multiplicity > 1) {
                        // A sequence of values
                        Object[] values = extractValuesFromCollection(attrValue, attrType);
                        if (values.length == multiplicity) {
                            attributes.put(attr, values);
                        } else {
                            DTLogger.warn(getChannel(),
                                    "Error saving output object " + objectTypeAndId + ": "
                                            + "attribute " + attr + " has " + values.length
                                            + " value(s), but we need " + multiplicity);
                        }
                    } else {
                        // A single value
                        Object result = attrType.fromUseToCypherObject(attrValue);
                        attributes.put(attr, result);
                    }
                } else {
                    DTLogger.warn(getChannel(),
                            "Error saving output object " + objectTypeAndId + ": "
                                    + "attribute " + attr + " not found in class " + retrievedClass);
                }
            }

            // Create the node
            Result newObject = tx.run("CREATE (o:" + nodeLabel + " $attributes) RETURN id(o)",
                    parameters("attributes", attributes));
            int nodeId = newObject.single().get("id(o)", 0);

            // Create relationships
            String twinId = useApi.getStringAttribute(objstate, "twinId");
            String executionId = useApi.getStringAttribute(objstate, "executionId");

            tx.run("MATCH (r:BraccioRobot), (o:" + nodeLabel + ") " +
                            "WHERE r.twinId = $twinId AND r.executionId = $executionId " +
                            "AND NOT r.isPhysical AND id(o) = $id " +
                            "CREATE (r)-[:" + edgeToRobotLabel + "]->(o)",
                    parameters(
                            "twinId", twinId,
                            "executionId", executionId,
                            "id", nodeId));

            DTNeo4jUtils.ensureTimestamp(tx, timestamp);

            tx.run("MATCH (o:" + nodeLabel + "), (t:Time) " +
                    "WHERE id(o) = $id AND t.timestamp = $timestamp " +
                    "CREATE (o)-[:AT_TIME]->(t)",
                    parameters(
                            "timestamp", timestamp,
                            "id", nodeId));

            createExtraRelationships(tx, nodeId, objstate);

            // Update timestamp
            DTNeo4jUtils.updateDTTimestampInDataLake(tx, timestamp);

            return null;
        });

        DTLogger.info(getChannel(), "Saved output object: " + objectTypeAndId);

        // Save the object
        int time = useApi.getCurrentTime();
        useApi.setAttribute(objstate, IS_PROCESSED, true);
        useApi.setAttribute(objstate, WHEN_PROCESSED, time);

        // Clean up
        try {
            cleanUpModel(objstate);
        } catch (Exception ex) {
            DTLogger.error(getChannel(), "Could not clean up model:");
            ex.printStackTrace();
        }
    }

    /**
     * Generates and returns an identifier for an object to be stored in the data lake.
     * @param objstate The object state to generate the identifier from.
     * @return The identifier for the object.
     */
    protected abstract String getObjectId(MObjectState objstate);

    /**
     * Override to implement connections of each node to create to other existing nodes.
     * @param tx A Neo4j transaction object.
     */
    protected abstract void createExtraRelationships(Transaction tx, int nodeId, MObjectState objstate);

    /**
     * Removes processed objects from the Data Lake.
     * @param objstate The object state that has been processed.
     */
    protected abstract void cleanUpModel(MObjectState objstate) throws UseApiException;

    /**
     * Converts an USE collection value to an array of values.
     * @param collection The collection value to convert.
     * @param baseType Type of each element in the collection.
     * @return An array of strings containing each value in the collection.
     */
    private Object[] extractValuesFromCollection(String collection, AttributeType baseType) {
        collection = collection.replaceAll("(?:Set|Bag|OrderedSet|Sequence)\\{(.*)}", "$1");
        String[] objects = collection.split(",");
        Object[] result = new Object[objects.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = baseType.fromUseToCypherObject(objects[i]);
        }
        return result;
    }

}
