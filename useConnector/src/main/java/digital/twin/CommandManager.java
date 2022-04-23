package digital.twin;

import pubsub.DTPubSub;

public class CommandManager extends InputManager {

    public CommandManager(DTUseFacade useApi) {
        super(useApi, DTPubSub.COMMAND_IN_CHANNEL, "Command", "DTCommand");
    }

}
