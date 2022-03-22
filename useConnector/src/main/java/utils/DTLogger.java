package utils;

import pubsub.PubService;

public class DTLogger {

    public static void info(String msg) {
        System.out.println("[DT-INFO] " + msg);
    }
    public static void info(PubService service, String msg) {
        System.out.println("[" + service.hashCode() + "-" + service.getChannel() + "] " + msg);
    }
    public static void warn(String msg) {
        System.err.println("[DT-WARN] " + msg);
    }
    public static void error(String msg) {
        System.err.println("[DT-ERR] " + msg);
    }

}
