package api;

public class AutoTicker implements Runnable {

    private static final int TICK_RESOLUTION_MS = 100;

    private volatile boolean running;
    private volatile boolean ticking;
    private final DTDLConnection connection;

    public AutoTicker(DTDLConnection connection) {
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
                    dl.advanceTime(TICK_RESOLUTION_MS);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void play() {
        ticking = true;
    }

    public void pause() {
        ticking = false;
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
