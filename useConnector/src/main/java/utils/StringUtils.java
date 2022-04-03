package utils;

/**
 * @author Daniel Pérez - University of Málaga
 * A collection of utility methods for string manipulation.
 */
public class StringUtils {

    /**
     * Removes all quote characters from a string.
     * @param str The string whose quotes to remove.
     * @return The string with its quotes removed.
     */
    public static String removeQuotes(String str) {
        return str.replaceAll("['\"]", "");
    }

}
