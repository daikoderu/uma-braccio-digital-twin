package utils;

import pubsub.PubService;

/**
 * @author Daniel Pérez - University of Málaga
 * A collection of utility methods for printing text to the console.
 */
public class DTLogger {

    /**
     * Prints a message to the standard output.
     * @param msg The message to print.
     */
    public static void info(String msg) {
        System.out.println("[DT-INFO] " + msg);
    }

    /**
     * Prints a warning message to the standard error output.
     * @param msg The message to print.
     */
    public static void warn(String msg) {
        System.err.println("[DT-WARN] " + msg);
    }

    /**
     * Prints an error message to the standard error output.
     * @param msg The message to print.
     */
    public static void error(String msg) {
        System.err.println("[DT-ERR] " + msg);
    }

    /**
     * Prints a message to the standard output.
     * @param tag A tag to prepend to the message.
     * @param msg The message to print.
     */
    public static void info(String tag, String msg) {
        System.out.println("[DT-INFO:" + tag + "] " + msg);
    }

    /**
     * Prints a warning message to the standard error output.
     * @param tag A tag to prepend to the message.
     * @param msg The message to print.
     */
    public static void warn(String tag, String msg) {
        System.err.println("[DT-WARN:" + tag + "] " + msg);
    }

    /**
     * Prints a message to the standard output.
     * @param service A PubService whose channel name to use as a tag to be prepended to the message.
     * @param msg The message to print.
     */
    public static void info(PubService service, String msg) {
        System.out.println("[" + service.getChannel() + "] " + msg);
    }

}
