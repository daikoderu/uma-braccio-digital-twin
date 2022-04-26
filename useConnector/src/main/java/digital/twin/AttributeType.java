package digital.twin;

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
    public String fromUseToRedisString(String value) {
        switch (this) {

            case BOOLEAN:
                return Boolean.parseBoolean(value) ? "1" : "0";

            case INTEGER:
            case REAL:
                return value;

            case STRING:
                return value.substring(1, value.length() - 1);

            default:
                return "undefined";

        }
    }

    /**
     * Converts a USE value string to a string to be stored on a Redis data lake.
     * @param value The Redis value to convert, as a string returned by the Data Lake.
     * @return The value to be passed to the USEFacade methods.
     */
    public Object fromRedisStringToObject(String value) {
        switch (this) {

            case BOOLEAN:
                return !value.equals("0");

            case INTEGER:
                return Integer.parseInt(value);

            case REAL:
                return Double.parseDouble(value);

            case STRING:
                return value;

            default:
                return null;

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
                return Double.parseDouble(value);

            default:
                return 0;

        }
    }

}
