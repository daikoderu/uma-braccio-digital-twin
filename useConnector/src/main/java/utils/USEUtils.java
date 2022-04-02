package utils;

import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;

import java.util.ArrayList;
import java.util.List;

public class USEUtils {

    /**
     * Returns all objects of a specific class in the USE model.
     * @param api The USE system API instance to interact with the currently displayed object diagram.
     * @param className The name of the class whose instances to retrieve.
     * @return A list with all objects of the specified class.
     */
    public static List<MObjectState> getObjectsOfClass(UseSystemApi api, String className) {
        List<MObjectState> result = new ArrayList<>();
        MClass mclass = api.getSystem().model().getClass(className);
        for (MObject o : api.getSystem().state().allObjects()) {
            if (o.cls().allSupertypes().contains(mclass)) {
                MObjectState ostate = o.state(api.getSystem().state());
                result.add(ostate);
            }
        }
        return result;
    }

    /**
     * Returns an attribute object of a specific class in the USE model.
     * @param api The USE system API instance to interact with the currently displayed object diagram.
     * @param className The name of the class whose attribute to retrieve.
     * @param attributeName The name of the attribute to retrieve.
     * @return The attribute object. It may be from the specified class or any of its superclasses.
     */
    public static MAttribute getAttribute(UseSystemApi api, String className, String attributeName) {
        MClass mclass = api.getSystem().model().getClass(className);
        return mclass.attribute(attributeName, true);
    }

    /**
     * Retrieves an attribute with the name <i>attributeName</i> from an USE object state.
     *
     * @param objstate State of the USE object.
     * @param attributeName Name of the attribute whose value is retrieved.
     * @return The corresponding attribute value, or null if the attribute is not found.
     */
    public static String getAttributeAsString(MObjectState objstate, String attributeName) {
        try {
            return objstate.attributeValue(attributeName).toString();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

}
