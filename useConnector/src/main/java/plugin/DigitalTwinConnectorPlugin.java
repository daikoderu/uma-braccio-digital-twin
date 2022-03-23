package plugin;

import org.tzi.use.runtime.IPluginRuntime;
import org.tzi.use.runtime.impl.Plugin;

/**
 * @author Paula Muñoz - University of Málaga
 *
 * This class is needed for the proper interaction between USE and the plugin.
 */
public class DigitalTwinConnectorPlugin extends Plugin {

    protected final String PLUGIN_ID = "useDigitalTwinConnector";

    public String getName() {
        return PLUGIN_ID;
    }

    public void run(IPluginRuntime pluginRuntime) {
        // Nothing to initialize
    }

    public static void main(String[] args) {
        // Empty main method to suppress IDE error messages in manifest file
    }

}
