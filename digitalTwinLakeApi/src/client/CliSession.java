package client;

import api.DTDLConnection;
import api.DTDataLake;
import org.javatuples.Pair;

import java.io.PrintStream;
import java.util.Scanner;

public class CliSession {

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int DEFAULT_PORT = 6379;

    private String twinId, executionId;
    private DTDLConnection connection;
    private final Scanner in;
    private final PrintStream out;
    private final PrintStream err;

    public CliSession() {
        out = System.out;
        err = System.err;
        in = new Scanner(System.in);
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
        out.print("Enter Twin Id: ");
        twinId = in.nextLine().trim();

        out.print("Enter Data Lake host and port (<host>[:<port>]): ");
        Pair<String, Integer> hostport;
        do {
            hostport = parseHostAndPort(in.nextLine().trim());
        } while (hostport == null);

        // Connect to the database
        connection = new DTDLConnection(hostport.getValue0(), hostport.getValue1());
        if (!checkConnectionWithDatabase()) {
            return EXIT_FAILURE;
        }

        // Obtain executionId
        try (DTDataLake dl = connection.getResource()) {
            executionId = dl.getCurrentExecutionId();
            if (executionId == null) {
                err.println("Error: executionId not found. Make sure the USE model is correctly initialized.");
                return EXIT_FAILURE;
            }
        } catch (Exception ex) {
            err.println("An error ocurred:");
            ex.printStackTrace();
            return EXIT_FAILURE;
        }

        return EXIT_SUCCESS;
    }

    private int runShell() {
        boolean quit = false;
        String[] tokens;
        while (!quit) {
            out.print(twinId + ":" + executionId + "> ");
            do {
                tokens = prompt();
            } while (tokens.length == 0 || tokens[0].isEmpty());

            if ("quit".equals(tokens[0])) {
                quit = true;
            } else {
                String[] args = new String[tokens.length - 1];
                System.arraycopy(tokens, 1, args, 0, tokens.length - 1);
                try (DTDataLake dl = connection.getResource()) {
                    dl.putCommand(twinId, tokens[0], args);
                } catch (Exception ex) {
                    err.println("An error ocurred:");
                    ex.printStackTrace();
                }
            }
        }
        return EXIT_SUCCESS;
    }

    private String[] prompt() {
        return in.nextLine().trim().split(" +");
    }

    private boolean checkConnectionWithDatabase() {
        try (DTDataLake dl = connection.getResource()) {
            return dl.ping();
        } catch (Exception ex) {
            err.println("Data lake connection error:");
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

}
