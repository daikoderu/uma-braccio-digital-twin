package api;

public class ClockController implements Runnable {

    private static final int TICK_RESOLUTION_MS = 100;

    private volatile boolean running;
    private volatile boolean ticking;
    private final DTDLConnection connection;

    public ClockController(DTDLConnection connection) {
        ticking = false;
        running = false;
        this.connection = connection;
    }

    public void run() {
        running = true;
        while (running) {
            busyWait();
            if (ticking) {
                try (DTDataLake dl = connection.getResource()) {
                    dl.advanceDTTime(TICK_RESOLUTION_MS);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean isTicking() {
        return ticking;
    }
    public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }

    public void stop() {
        running = false;
    }

    private void busyWait() {
        try {
            Thread.sleep(TICK_RESOLUTION_MS);
        } catch (InterruptedException ignored) { }
    }

}
