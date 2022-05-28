package api;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Class that contains all the information associated to a command and its response.
 * Attribute receivedAt contains the timestamp at which the command was sent to the corresponding twin,
 * or -1 if it was not sent.
 * Attribute finishedAt contains when the command actually finished its execution, or -1 if it didn't finish.
 */
@SuppressWarnings("unused")
public class Command {

    private int receivedAt; // -1 if the command is not received by the twin
    private int finishedAt; // -1 if the command execution has not finished
    private String twinId;
    private String executionId;
    private String name;
    private String[] arguments;
    private int commandId;
    private String result;

    private Command() { }

    /**
     * Deserializes a command and its result object from the Data Lake into a Command Java object.
     * @param commandHash The command object to deserialize.
     * @param resultHash The result object to deserialize.
     * @return The resulting snapshot, or null if the hash does not represent a valid snapshot.
     */
    static Command fromHashes(Map<String, String> commandHash, Map<String, String> resultHash) {
        Command result = new Command();
        try {
            result.twinId = commandHash.get("twinId");
            result.executionId = commandHash.get("executionId");
            result.name = commandHash.get("name");
            result.arguments = commandHash.get("arguments").trim().split(" +");
            result.commandId = Integer.parseInt(commandHash.get("commandId"));
            if (commandHash.containsKey("whenProcessed")) {
                result.receivedAt = Integer.parseInt(commandHash.get("whenProcessed"));
            } else {
                result.receivedAt = -1;
            }
            if (resultHash != null) {
                result.result = resultHash.get("return");
                result.finishedAt = Integer.parseInt(resultHash.get("timestamp"));
            } else {
                result.result = null;
                result.finishedAt = -1;
            }
            return result;
        } catch (NumberFormatException | NullPointerException ex) {
            return null;
        }
    }

    public int getReceivedAt() {
        return receivedAt;
    }
    public int getFinishedAt() {
        return finishedAt;
    }
    public String getTwinId() {
        return twinId;
    }
    public String getExecutionId() {
        return executionId;
    }
    public String getName() {
        return name;
    }
    public String getArgument(int index) {
        return arguments[index];
    }
    public int getArgumentCount() {
        return arguments.length;
    }
    public int getCommandId() {
        return commandId;
    }
    public String getResult() {
        return result;
    }
    public boolean isReceived() {
        return receivedAt != -1;
    }
    public boolean isFinished() {
        return finishedAt != -1;
    }

    public String toString() {
        StringJoiner args = new StringJoiner(" ");
        for (String arg : arguments) {
            args.add(arg);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Command:").append(twinId).append(":").append(executionId).append(":").append(commandId);
        builder.append("('").append(name).append(" ").append(args).append("')");
        if (isReceived()) {
            builder.append("(receivedAt=").append(receivedAt);
            if (isFinished()) {
                builder.append(", finishedAt=").append(finishedAt);
            }
            builder.append(")");
        }
        return builder.toString();
    }

}
