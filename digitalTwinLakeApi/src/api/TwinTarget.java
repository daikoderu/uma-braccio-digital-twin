package api;

/**
 * @author Daniel Pérez - University of Málaga
 * Enumeration to specify to what twin (the Physical Twin, the Digital Twin,
 * both or neither) a command refers to.
 */
@SuppressWarnings("unused")
public enum TwinTarget {

    NONE        (false, false),
    PHYSICAL    (true,  false),
    DIGITAL     (false, true),
    BOTH        (true,  true);

    public final boolean isPhysical;
    public final boolean isDigital;

    TwinTarget(boolean isPhysical, boolean isDigital) {
        this.isPhysical = isPhysical;
        this.isDigital = isDigital;
    }

    void requireOneTwin() {
        if (this != PHYSICAL && this != DIGITAL) {
            throw new IllegalArgumentException("Target must be PHYSICAL or DIGITAL");
        }
    }

    String getPrefix() {
        requireOneTwin();
        return this == PHYSICAL ? "PT" : "DT";
    }

}
