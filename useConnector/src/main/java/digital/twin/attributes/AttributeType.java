package digital.twin.attributes;

import utils.StringUtils;

public enum AttributeType {

    BOOLEAN, NUMBER, STRING;

    /**
     * Converts a USE value string to a string to be stored on a Redis data lake.
     * @param value The USE value to convert, as a string returned by the USE API.
     * @return The value to be stored in Redis.
     */
    public String toRedisString(String value) {
        switch (this) {

            case BOOLEAN:
                return Boolean.parseBoolean(value) ? "1" : "0";

            case NUMBER:
                return value;

            case STRING:
                return StringUtils.removeQuotes(value);

            default:
                return "???";

        }
    }

}
