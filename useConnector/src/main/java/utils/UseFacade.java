package utils;

import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.ocl.expr.ExpObjRef;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.ExpressionWithValue;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.value.*;
import org.tzi.use.uml.sys.*;
import org.tzi.use.uml.sys.soil.MObjectOperationCallStatement;
import org.tzi.use.uml.sys.soil.MOperationCallStatement;
import org.tzi.use.uml.sys.soil.MStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Pérez - University of Málaga
 * A collection of utility methods for USE model manipulation.
 */
@SuppressWarnings("unused")
public class UseFacade {

    private final UseSystemApi api;

    /**
     * Sets the API instance to use for all subsequent calls to UseFacade methods.
     * @param api The USE API instance to interact with the currently displayed object diagram.
     */
    public UseFacade(UseSystemApi api) {
        this.api = api;
    }

    public void updateDerivedValues() {
        api.getSystem().state().updateDerivedValues(true);
    }

    // Object Creation
    // ============================================================================================

    public synchronized MObjectState createObject(String className, String objectName)
            throws MSystemException {
        MClass mclass = api.getSystem().model().getClass(className);
        MSystemState state = api.getSystem().state();
        return state.createObject(mclass, objectName).state(state);
    }

    // Object Searching
    // ============================================================================================

    /**
     * Returns all objects of a specific class in the USE model.
     * @param className The name of the class whose instances to retrieve.
     * @return A list with all objects of the specified class.
     */
    public synchronized List<MObjectState> getObjectsOfClass(String className) {
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
     * Returns any object of a specific class in the USE model.
     * @param className The name of the class whose instance to retrieve.
     * @return An instance of the given class, or null if no instances are found.
     */
    public synchronized MObjectState getAnyObjectOfClass(String className) {
        MObjectState result = null;
        MClass mclass = api.getSystem().model().getClass(className);
        for (MObject o : api.getSystem().state().allObjects()) {
            if (o.cls().allSupertypes().contains(mclass)) {
                result = (o.state(api.getSystem().state()));
                break;
            }
        }
        return result;
    }

    // Attribute Getters
    // ============================================================================================

    /**
     * Returns the value of an integer attribute in the model.
     * @param objstate The state of the object whose attribute to retrieve.
     * @param attributeName The name of the attribute to retrieve.
     * @return The value of the attribute, or null if the value is not an integer.
     */
    public synchronized Integer getIntegerAttribute(MObjectState objstate, String attributeName) {
        Value v = objstate.attributeValue(attributeName);
        return v instanceof IntegerValue ? ((IntegerValue) v).value() : null;
    }

    /**
     * Returns the value of a real number attribute in the model.
     * @param objstate The state of the object whose attribute to retrieve.
     * @param attributeName The name of the attribute to retrieve.
     * @return The value of the attribute, or null if the value is not a real number.
     */
    public synchronized Double getRealAttribute(MObjectState objstate, String attributeName) {
        Value v = objstate.attributeValue(attributeName);
        return v instanceof RealValue ? ((RealValue) v).value() : null;
    }

    /**
     * Returns the value of a string attribute in the model.
     * @param objstate The state of the object whose attribute to retrieve.
     * @param attributeName The name of the attribute to retrieve.
     * @return The value of the attribute, or null if the value is not a string.
     */
    public synchronized String getStringAttribute(MObjectState objstate, String attributeName) {
        Value v = objstate.attributeValue(attributeName);
        return v instanceof StringValue ? ((StringValue) v).value() : null;
    }

    /**
     * Returns the value of a boolean attribute in the model.
     * @param objstate The state of the object whose attribute to retrieve.
     * @param attributeName The name of the attribute to retrieve.
     * @return The value of the attribute, or null if the value is not a boolean value.
     */
    public synchronized Boolean getBooleanAttribute(MObjectState objstate, String attributeName) {
        Value v = objstate.attributeValue(attributeName);
        return v instanceof BooleanValue ? ((BooleanValue) v).value() : null;
    }

    /**
     * Returns the value of any attribute as a string.
     * @param objstate The state of the object whose attribute to retrieve.
     * @param attributeName The name of the attribute to retrieve.
     * @return The value of the attribute.
     */
    public synchronized String getAttributeAsString(MObjectState objstate, String attributeName) {
        return objstate.attributeValue(attributeName).toString();
    }

    // Attribute Setters
    // ============================================================================================

    /**
     * Sets the value of attribute <i>attributeName</i>.
     * @param objstate The object whose attribute to set.
     * @param attributeName The name of the attribute to set.
     * @param value The value to set.
     */
    public synchronized void setAttribute(MObjectState objstate, String attributeName, Object value) {
        setAttributeAux(objstate, attributeName, objectToUseValue(value));
    }

    @SuppressWarnings("unchecked")
    private Value objectToUseValue(Object object) {
        if (object instanceof Integer) {
            return IntegerValue.valueOf((int) object);
        } else if (object instanceof Double) {
            return new RealValue((double) object);
        } else if (object instanceof String) {
            return new StringValue((String) object);
        } else if (object instanceof Boolean) {
            return BooleanValue.get((boolean) object);
        } else if (object instanceof List) {
            List<Object> list = (List<Object>) object;
            Type type = null;
            boolean multipleTypes = false;
            List<Value> useValues = new ArrayList<>();
            for (Object listobj : list) {
                Value newValue = objectToUseValue(listobj);
                if (type != null) {
                    if (!type.equals(newValue.type())) {
                        multipleTypes = true;
                        break;
                    }
                } else {
                    type = newValue.type();
                }
                useValues.add(newValue);
            }
            if (type != null && !multipleTypes) {
                return new SequenceValue(type, useValues);
            } else {
                throw new RuntimeException("Cannot convert this Java object to a USE value");
            }
        } else if (object == null) {
            return UndefinedValue.instance;
        } else {
            throw new RuntimeException("Cannot convert this Java object to a USE value");
        }
    }

    private void setAttributeAux(MObjectState objstate, String attributeName, Value value) {
        MClass mclass = objstate.object().cls();
        MAttribute attribute = mclass.attribute(attributeName, true);
        objstate.setAttributeValue(attribute, value);
    }

    // Operation calls
    // ============================================================================================

    public synchronized void callOperation(MObjectState objstate, String operationName, Object[] args)
            throws MSystemException {
        MObject mobject = objstate.object();
        MClass mclass = mobject.cls();
        MOperation operation = mclass.operation(operationName, true);
        Expression[] useArgs = new Expression[args.length];
        for (int i = 0; i < args.length; i++) {
            useArgs[i] = new ExpressionWithValue(objectToUseValue(args[i]));
        }
        MStatement stmt = new MObjectOperationCallStatement(
                new ExpObjRef(mobject), operation, useArgs);
        api.getSystem().execute(stmt);
    }

}
