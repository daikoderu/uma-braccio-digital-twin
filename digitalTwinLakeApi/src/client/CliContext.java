package client;

import api.ClockController;
import api.DTDLConnection;

import java.io.PrintStream;
import java.util.Scanner;

public class CliContext {

    public PrintStream out;
    public PrintStream err;
    public Scanner in;

    String twinId, executionId;
    DTDLConnection connection;
    public boolean quitting;

    ClockController clockController;
    Thread clockControllerThread;

    CliContext() {
        out = System.out;
        err = System.err;
        in = new Scanner(System.in);
        twinId = null;
        executionId = null;
        connection = null;
        quitting = false;

        clockController = null;
        clockControllerThread = null;
    }

}
