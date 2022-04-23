package digital.twin.attributes;

import utils.StringUtils;

/**
 * @author Daniel Pérez - University of Málaga
 * Types of each attribute in an AttributeSpecification.
 */
public enum AttributeType {

    BOOLEAN, INTEGER, REAL, STRING;

    /**
     * Converts a USE value string to a string to be stored on a Redis data lake.
     * @param value The USE value to convert, as a string returned by the USE API.
     * @return The value to be stored in Redis.
     */
    public String toRedisString(String value) {
        switch (this) {

            case BOOLEAN:
                return Boolean.parseBoolean(value) ? "1" : "0";

            case INTEGER:
            case REAL:
                return value;

            case STRING:
                return StringUtils.removeQuotes(value);

            default:
                return "???";

        }
    }

    /**
     * Returns the score to give to a Redis value according to this type.
     * @param value The Redis value to convert whose score to calculate.
     * @return The resulting score.
     */
    public double getScore(String value) {
        switch (this) {

            case BOOLEAN:
                return Boolean.parseBoolean(value) ? 1 : 0;

            case INTEGER:
            case REAL:
                return Double.parseDouble(value.replace("'", ""));

            default:
                return 0;

        }
    }

}
