package plugin;

public class DriverConfig {

    // Milliseconds to wait between checks for commands and snapshots.
    public static final long SLEEP_TIME_MS = 2000;

    // Milliseconds to wait between tick checks.
    public static final int TICK_PERIOD_MS = 100;

    // Number of threads to use.
    public static final int NUM_EXECUTOR_POOL_THREADS = 10;

    // Hostname of the Redis instance that contains the data lake.
    public static final String REDIS_HOSTNAME = "localhost";

}
