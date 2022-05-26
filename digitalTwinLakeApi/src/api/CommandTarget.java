package api;

/**
 * @author Daniel Pérez - University of Málaga
 * Enumeration to specify to what twin (the Physical Twin, the Digital Twin,
 * both or neither) a command refers to.
 */
@SuppressWarnings("unused")
public enum CommandTarget {

    NONE        (false, false),
    PHYSICAL    (true,  false),
    DIGITAL     (false, true),
    BOTH        (true,  true);

    public final boolean isPhysical;
    public final boolean isDigital;

    CommandTarget(boolean isPhysical, boolean isDigital) {
        this.isPhysical = isPhysical;
        this.isDigital = isDigital;
    }

}
