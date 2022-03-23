package utils;

import pubsub.PubService;

public class DTLogger {

    public static void info(String msg) {
        System.out.println("[DT-INFO] " + msg);
    }
    public static void warn(String msg) {
        System.err.println("[DT-WARN] " + msg);
    }
    public static void error(String msg) {
        System.err.println("[DT-ERR] " + msg);
    }

    public static void info(String tag, String msg) {
        System.out.println("[DT-INFO:" + tag + "] " + msg);
    }
    public static void warn(String tag, String msg) {
        System.err.println("[DT-WARN:" + tag + "] " + msg);
    }
    public static void error(String tag, String msg) {
        System.err.println("[DT-ERR:" + tag + "] " + msg);
    }

    public static void info(PubService service, String msg) {
        System.out.println("[" + service.getChannel() + "] " + msg);
    }

}
