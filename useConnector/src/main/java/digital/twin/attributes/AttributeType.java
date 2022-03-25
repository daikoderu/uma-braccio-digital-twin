package digital.twin.attributes;

import utils.StringUtils;

public enum AttributeType {

    BOOLEAN, NUMBER, STRING;

    public double getSearchRegisterScore(String value) {
        switch (this) {
            case NUMBER:
                return Double.parseDouble(value.replace("'", ""));

            case BOOLEAN:
                return Boolean.parseBoolean(value) ? 1 : 0;

            default:
                return 0;
        }
    }

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
