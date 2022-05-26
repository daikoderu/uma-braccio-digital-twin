package api;

import java.util.Map;

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
            result.isMoving = !hash.get("moving").equals("0");
            return result;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

}
