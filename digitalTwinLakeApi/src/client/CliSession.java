package client;

import api.ClockController;
import api.CommandTarget;
import api.DTDLConnection;
import api.DTDataLake;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CliSession {

    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_FAILURE = 1;
    public static final int DEFAULT_PORT = 6379;

    private final CliContext context;
    private final Map<String, BiConsumer<String[], CliContext>> commandTypes;

    public CliSession() {
        context = new CliContext();
        commandTypes = new HashMap<>();
        loadShellCommands();
    }

    public int start() {
        int exitcode = initialize();
        if (exitcode == EXIT_SUCCESS) {
            return runShell();
        } else {
            return exitcode;
        }
    }

    private int initialize() {
        context.out.print("Enter Twin Id: ");
        context.twinId = context.in.nextLine().trim();

        context.out.print("Enter Data Lake host and port (<host>[:<port>]): ");
        Pair<String, Integer> hostport;
        do {
            hostport = parseHostAndPort(context.in.nextLine().trim());
        } while (hostport == null);

        // Connect to the database
        context.connection = new DTDLConnection(hostport.getValue0(), hostport.getValue1());
        if (!checkConnectionWithDatabase()) {
            return EXIT_FAILURE;
        }

        // Obtain executionId
        try (DTDataLake dl = context.connection.getResource()) {
            context.executionId = dl.getCurrentExecutionId();
            if (context.executionId == null) {
                context.err.println("Error: executionId not found. " +
                        "Make sure the USE model is correctly initialized.");
                return EXIT_FAILURE;
            }
        } catch (Exception ex) {
            context.err.println("An error ocurred:");
            ex.printStackTrace();
            return EXIT_FAILURE;
        }

        createClockController();
        context.out.println("Connection with Data Lake successful! Now you can send commands to the twins.");
        context.out.println("Type 'quit' to quit.");
        return EXIT_SUCCESS;
    }

    private int runShell() {
        while (!context.quitting) {
            String[] tokens;
            context.out.print(context.twinId + ":" + context.executionId + "> ");
            do {
                tokens = prompt();
            } while (tokens.length == 0 || tokens[0].isEmpty());

            String[] args = new String[tokens.length - 1];
            System.arraycopy(tokens, 1, args, 0, tokens.length - 1);

            if (commandTypes.containsKey(tokens[0])) {
                commandTypes.get(tokens[0]).accept(args, context);
            } else {
                try (DTDataLake dl = context.connection.getResource()) {
                    int commandId = dl.putCommand(context.twinId, CommandTarget.BOTH, tokens[0], args);
                    context.out.println("Command sent to both twins with ID = " + commandId);
                } catch (Exception ex) {
                    context.err.println("An error ocurred:");
                    ex.printStackTrace();
                }
            }
        }
        return EXIT_SUCCESS;
    }

    private String[] prompt() {
        return context.in.nextLine().trim().split(" +");
    }

    private boolean checkConnectionWithDatabase() {
        try (DTDataLake dl = context.connection.getResource()) {
            return dl.ping();
        } catch (Exception ex) {
            context.err.println("Data lake connection error:");
            ex.printStackTrace();
            return false;
        }
    }

    private Pair<String, Integer> parseHostAndPort(String input) {
        int colonIndex = input.lastIndexOf(':');
        if (colonIndex != -1) {
            try {
                String host = input.substring(0, colonIndex);
                int port = Integer.parseInt(input.substring(colonIndex + 1));
                if (port >= 0 && port <= 65535) {
                    return new Pair<>(host, port);
                } else {
                    return null;
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        } else {
            return new Pair<>(input, DEFAULT_PORT);
        }
    }

    private void createClockController() {
        context.clockController = new ClockController(context.connection);
        context.clockControllerThread = new Thread(context.clockController);
        context.clockControllerThread.start();
    }

    private void loadShellCommands() {
        commandTypes.put("quit", (args, context1) -> ShellCommands.quit(context1));
        commandTypes.put("dtclock", ShellCommands::dtclock);
    }

}
