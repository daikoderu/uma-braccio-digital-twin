package digital.twin;

import digital.twin.attributes.AttributeType;
import pubsub.DTPubSub;

/**
 * @author Paula Muñoz, Daniel Pérez - University of Málaga
 * OutputManager that retrieves all OutputSnapshot instances and serializes them for storage in the data lake.
 */
public class OutputSnapshotsManager extends OutputManager {

    private static final int NUMBER_OF_SERVOS = 6;

    /**
     * Default constructor.
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     */
    public OutputSnapshotsManager(DTUseFacade useApi) {
        super(useApi, DTPubSub.DT_OUT_CHANNEL, "OutputBraccioSnapshot", "DTOutputSnapshot");
        attributeSpecification.set("currentAngles", AttributeType.REAL, NUMBER_OF_SERVOS);
        attributeSpecification.set("targetAngles", AttributeType.REAL, NUMBER_OF_SERVOS);
        attributeSpecification.set("currentSpeeds", AttributeType.REAL, NUMBER_OF_SERVOS);
        attributeSpecification.set("moving", AttributeType.BOOLEAN);
    }

}
