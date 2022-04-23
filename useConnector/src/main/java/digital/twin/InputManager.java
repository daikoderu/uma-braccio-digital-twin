package digital.twin;

public abstract class InputManager {

    protected final DTUseFacade useApi;
    private final String channel;
    private final String targetClass;
    private final String objectType;

    /**
     * Default constructor.
     * @param useApi USE API facade instance to interact with the currently displayed object diagram.
     * @param channel The channel this InputManager is created from.
     * @param targetClass The USE class that will contain the deserialized instances.
     * @param objectType The type of the Data Lake objects to deserialize.
     */
    public InputManager(DTUseFacade useApi, String channel, String targetClass, String objectType) {
        this.useApi = useApi;
        this.channel = channel;
        this.targetClass = targetClass;
        this.objectType = objectType;
    }

}
