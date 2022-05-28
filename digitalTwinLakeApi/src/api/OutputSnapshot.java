package api;

import java.util.Map;

/**
 * Class that contains all the information associated to an output snapshot.
 */
@SuppressWarnings("unused")
public class OutputSnapshot {

    private int timestamp;
    private String twinId;
    private String executionId;
    private Position currentAngles;
    private Position targetAngles;
    private ServoVector currentSpeeds;
    private boolean isMoving;

    private OutputSnapshot() { }

    /**
     * Deserializes a snapshot from the Data Lake into an OutputSnapshot Java object.
     * @param hash The object to deserialize.
     * @return The resulting snapshot, or null if the hash does not represent a valid snapshot.
     */
    static OutputSnapshot fromHash(Map<String, String> hash) {
        OutputSnapshot result = new OutputSnapshot();
        try {
            result.timestamp = Integer.parseInt(hash.get("timestamp"));
            result.twinId = hash.get("twinId");
            result.executionId = hash.get("executionId");
            result.currentAngles = new Position();
            result.targetAngles = new Position();
            result.currentSpeeds = new ServoVector();
            for (int i = 0; i < 6; i++) {
                float currentAngle = Float.parseFloat(hash.get("currentAngles_" + (i + 1)));
                int targetAngle = Integer.parseInt(hash.get("targetAngles_" + (i + 1)));
                float currentSpeed = Float.parseFloat(hash.get("currentSpeeds_" + (i + 1)));
                result.currentAngles.set(i, Math.round(currentAngle));
                result.currentAngles.set(i, targetAngle);
                result.currentAngles.set(i, Math.round(currentSpeed));
            }
            result.isMoving = !hash.get("moving").equals("0");
            return result;
        } catch (NumberFormatException | NullPointerException ex) {
            return null;
        }
    }

    public int getTimestamp() {
        return timestamp;
    }
    public String getTwinId() {
        return twinId;
    }
    public String getExecutionId() {
        return executionId;
    }
    public Position getCurrentAngles() {
        return new Position(currentAngles);
    }
    public Position getTargetAngles() {
        return new Position(targetAngles);
    }
    public ServoVector getCurrentSpeeds() {
        return new ServoVector(currentSpeeds);
    }
    public boolean isMoving() {
        return isMoving;
    }

    public String toString() {
        return "OutputSnapshot:" + twinId + ":" + executionId + ":" + timestamp
                + "(" + currentAngles + ", " + targetAngles + ", " + currentSpeeds + ")";
    }

}
