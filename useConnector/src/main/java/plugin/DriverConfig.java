package plugin;

public class DriverConfig {

    // Milliseconds to wait between checks for commands and snapshots.
    public static final long SLEEP_TIME_MS = 2000;

    // Milliseconds to wait between tick checks.
    public static final int TICK_PERIOD_MS = 100;

    // Hostname of the Neo4j instance that contains the data lake.
    public static final String NEO4J_HOSTNAME = "localhost";

}
