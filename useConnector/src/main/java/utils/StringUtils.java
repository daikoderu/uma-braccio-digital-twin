package utils;

public class StringUtils {

    public static String removeQuotes(String str) {
        return str.replaceAll("['\"]", "");
    }

}
