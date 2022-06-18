package client;

import api.ClockController;
import api.DTDLConnection;
import api.DTDataLake;

import java.util.Objects;
import java.util.Scanner;

public class CliContext {

    private final Scanner in;

    private String twinId;
    private String executionId;
    DTDLConnection dtdlConnection;
    private boolean quitting;

    private ClockController clockController;
    private Thread clockControllerThread;

    CliContext() {
        in = new Scanner(System.in);
        twinId = null;
        executionId = null;
        quitting = false;
        clockController = null;
        clockControllerThread = null;
    }

    public void print(String info) {
        System.out.println(info);
    }
    public void error(String info) {
        System.out.println(info);
    }
    public String input(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    public String getTwinId() {
        return twinId;
    }
    public void setTwinId(String twinId) {
        this.twinId = twinId;
    }
    public String getExecutionId() {
        return executionId;
    }
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setupConnectionWithDataLake(String host, int port) {
        dtdlConnection = new DTDLConnection(host, port);
    }
    public DTDataLake getDataLake() {
        return dtdlConnection.getDataLake();
    }

    public void createClockController() {
        Objects.requireNonNull(dtdlConnection);
        if (clockController == null) {
            clockController = new ClockController(dtdlConnection);
            clockControllerThread = new Thread(clockController);
            clockControllerThread.start();
        }
    }
    public void stopClockController() {
        clockController.stop();
        try {
            clockControllerThread.join();
            clockController = null;
        } catch (InterruptedException ignored) { }
    }
    public void setClockControllerTicking(boolean ticking) {
        clockController.setTicking(ticking);
    }

    public boolean isQuitting() {
        return quitting;
    }
    public void setQuitting(boolean quitting) {
        this.quitting = quitting;
    }

}
