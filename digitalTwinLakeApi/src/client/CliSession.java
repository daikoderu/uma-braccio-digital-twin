package client;

import api.TwinTarget;
import api.DTDataLake;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

public class CliSession {

    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_FAILURE = 1;
    public static final int DEFAULT_PORT = 7687;

    private final CliContext ctx;
    private final Map<String, BiConsumer<String[], CliContext>> commandTypes;

    public CliSession() {
        ctx = new CliContext();
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
        ctx.setTwinId(ctx.input("Enter Twin Id: "));

        Pair<String, Integer> hostport;
        do {
            hostport = parseHostAndPort(ctx.input("Enter Data Lake host and port (<host>[:<port>]): "));
        } while (hostport == null);

        // Connect to the database
        ctx.setupConnectionWithDataLake(hostport.getValue0(), hostport.getValue1());
        if (!checkConnectionWithDatabase()) {
            ctx.error("Error: cannot connect to Data Lake.");
            return EXIT_FAILURE;
        }

        // Obtain executionId
        try (DTDataLake dl = ctx.getDataLake()) {
            ctx.setExecutionId(dl.getCurrentExecutionId());
            if (ctx.getExecutionId() == null) {
                ctx.error("Error: executionId not found. " +
                        "Make sure the USE model is correctly initialized.");
                return EXIT_FAILURE;
            }
        } catch (Exception ex) {
            ctx.error("An error ocurred:");
            ex.printStackTrace();
            return EXIT_FAILURE;
        }

        ctx.createClockController();
        ctx.print("Connection with Data Lake successful! Now you can send commands to the twins.");
        ctx.print("Type 'quit' to quit.");
        return EXIT_SUCCESS;
    }

    private int runShell() {
        while (!ctx.isQuitting()) {
            String[] tokens = prompt();
            String[] args = new String[tokens.length - 1];
            System.arraycopy(tokens, 1, args, 0, tokens.length - 1);

            if (commandTypes.containsKey(tokens[0])) {
                commandTypes.get(tokens[0]).accept(args, ctx);
            } else {
                try (DTDataLake dl = ctx.getDataLake()) {
                    int commandId = dl.forTwin(ctx.getTwinId()).putCommand(TwinTarget.BOTH, tokens[0], args);
                    ctx.print("Command sent to both twins with ID = " + commandId);
                } catch (Exception ex) {
                    ctx.print("An error ocurred:");
                    ex.printStackTrace();
                }
            }
        }
        return EXIT_SUCCESS;
    }

    private String[] prompt() {
        try {
            String[] tokens;
            String prompt = ctx.getTwinId() + ":" + ctx.getExecutionId() + "> ";
            do {
                tokens = ctx.input(prompt).split(" +");
            } while (tokens.length == 0 || tokens[0].isEmpty());
            return tokens;
        } catch (NoSuchElementException ex) {
            ctx.setQuitting(true);
            ctx.print("Quitting...");
            return new String[]{"quit"};
        }
    }

    private boolean checkConnectionWithDatabase() {
        try (DTDataLake dl = ctx.getDataLake()) {
            return dl.ping();
        } catch (Exception ex) {
            ctx.error("Data lake connection error:");
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

    private void loadShellCommands() {
        commandTypes.put("quit", (args, context1) -> ShellCommands.quit(context1));
        commandTypes.put("dtclock", ShellCommands::dtclock);
        commandTypes.put("test", ShellCommands::test);
    }

}
