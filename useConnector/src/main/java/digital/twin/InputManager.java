package digital.twin;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.tzi.use.uml.sys.MObjectState;
import utils.DTLogger;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * Class that retrieves all objects from a data lake and converts them to USE objects.
 */
public abstract class InputManager {

    protected static final String TIMESTAMP = "timestamp";

    private static int instanceCounter = 0;

    protected final AttributeSpecification attributeSpecification;
    protected final DTUseFacade useApi;
    private final String channel;
    private final String nodeLabel;
    private final String edgeToRobotLabel;

    /**
     * Default constructor.
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     * @param channel The channel this InputManager is created from.
     * @param nodeLabel The type of the Data Lake objects to deserialize.
     */
    public InputManager(DTUseFacade useApi, String channel, String nodeLabel, String edgeToRobotLabel) {
        attributeSpecification = new AttributeSpecification();
        this.useApi = useApi;
        this.channel = channel;
        this.nodeLabel = nodeLabel;
        this.edgeToRobotLabel = edgeToRobotLabel;
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
    public List<Record> getUnprocessedDLObjects(Session session) {
        return session.readTransaction(tx -> {
            Result result = tx.run("MATCH (r:BraccioRobot)-[:" + edgeToRobotLabel + "]->(i:" + nodeLabel + ") " +
                    "WHERE NOT r.isPhysical AND NOT i.isProcessed " +
                    "RETURN r.twinId, r.executionId, id(i), i ORDER BY " + getOrdering());
            List<Record> records = new ArrayList<>();
            while (result.hasNext()) {
                records.add(result.next());
            }
            return records;
        });
    }

    /**
     * Saves all the objects in the Data Lake to the USE model.
     */
    public void saveObjectsToUseModel(Session session, List<Record> records) {
        for (Record rec : records) {
            saveOneObject(rec, session);
        }
    }

    /**
     * Returns the name of the class a Node should be converted to.
     * @param rec The node to convert.
     * @return The name of the class.
     */
    protected abstract String getTargetClass(Record rec);

    /**
     * Auxiliary method to store the object in the USE model, extracted from the Data Lake.
     * @param rec The node to store in the USE model.
     */
    private synchronized void saveOneObject(Record rec, Session session) {
        String key = nodeLabel + ":" + getObjectId(rec);
        int nodeId = rec.get("id(i)").asInt();
        int timestamp = useApi.getCurrentTime();
        try {
            MObjectState objstate = useApi.createObject(
                    getTargetClass(rec), nodeLabel + ++instanceCounter);
            Node i = rec.get("i").asNode();
            for (String attr : attributeSpecification.attributeNames()) {
                int multiplicity = attributeSpecification.multiplicityOf(attr);
                if (multiplicity > 1) {
                    List<Object> values = i.get(attr).asList();
                    int numberOfValues = values.size();
                    if (numberOfValues == multiplicity) {
                        useApi.setAttribute(objstate, attr, values);
                    } else {
                        DTLogger.warn(getChannel(),
                                "Error saving input object " + key + ": "
                                        + "attribute " + attr + " has " + numberOfValues
                                        + " value(s), but we need " + multiplicity);
                    }
                } else {
                    Object value = i.get(attr).asObject();
                    useApi.setAttribute(objstate, attr, value);
                }
            }

            // Set twin, execution ID and timestamp
            useApi.setAttribute(objstate, "twinId", rec.get("r.twinId").asString());
            useApi.setAttribute(objstate, "executionId", rec.get("r.executionId").asString());
            useApi.setAttribute(objstate, TIMESTAMP, timestamp);

            DTLogger.info(getChannel(), "Saved input object: " + key);
            useApi.updateDerivedValues();
        } catch (Exception ex) {
            DTLogger.error(getChannel(), "Could not create object: " + ex.getMessage());
        }

        // Move object from the "UNPROCESSED" queue to the "PROCESSED" queue, and
        // set whenProcessed to indicate when this instance has been saved to the USE model.
        session.writeTransaction(tx -> {
            DTNeo4jUtils.ensureTimestamp(tx, timestamp);
            tx.run("MATCH (i:" + nodeLabel + "), (t:Time) " +
                "WHERE id(i) = $id AND t.timestamp = $time " +
                "CREATE (i)-[:AT_TIME]->(t) " +
                "SET i.isProcessed = true",
                parameters("id", nodeId, "time", timestamp));
            return null;
        });
    }

    /**
     * Generates and returns an identifier to display in the USE console.
     * @param rec The record to generate the identifier from.
     * @return The identifier for the object.
     */
    protected abstract String getObjectId(Record rec);

    /**
     * Returns the contents of the ORDER BY clause used to sort the nodes returned from
     * getUnprocessedDLObjects.
     * @return The ORDER BY clause, e.g. i.commandId, to return commands ordered by commandId.
     */
    protected abstract String getOrdering();

}
