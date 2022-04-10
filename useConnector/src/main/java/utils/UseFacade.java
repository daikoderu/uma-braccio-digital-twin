package utils;

import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.value.StringValue;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Pérez - University of Málaga
 * A collection of utility methods for USE model manipulation.
 */
public class UseFacade {

    private static UseSystemApi api;

    /**
     * Sets the API instance to use for all subsequent calls to USEUtils methods.
     * @param api The USE API instance to interact with the currently displayed object diagram.
     */
    public UseFacade(UseSystemApi api) {
        UseFacade.api = api;
    }

    /**
     * Returns all objects of a specific class in the USE model.
     * @param className The name of the class whose instances to retrieve.
     * @return A list with all objects of the specified class.
     */
    public List<MObjectState> getObjectsOfClass(String className) {
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
     * Retrieves an attribute with the name <i>attributeName</i> from an USE object state.
     * @param objstate State of the USE object.
     * @param attributeName Name of the attribute whose value is retrieved.
     * @return The corresponding attribute value, or null if the attribute is not found.
     */
    public String getAttributeAsString(MObjectState objstate, String attributeName) {
        try {
            return objstate.attributeValue(attributeName).toString();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public void setAttribute(MObjectState objstate, String attributeName, String value) {
        setAttributeAux(objstate, attributeName, new StringValue(value));
    }
    public void setAttribute(MObjectState objstate, String attributeName, boolean value) {
        setAttributeAux(objstate, attributeName, BooleanValue.get(value));
    }

    private void setAttributeAux(MObjectState objstate, String attributeName, Value value) {
        MClass objClass = objstate.object().cls();
        MAttribute attribute = objClass.attribute(attributeName, true);
        objstate.setAttributeValue(attribute, value);
    }

}
