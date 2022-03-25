package utils;

import org.tzi.use.api.UseSystemApi;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;

import java.util.ArrayList;
import java.util.List;

public class USEUtils {

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

    public static MObjectState getAnyObjectOfClass(UseSystemApi api, String className) {
        MObjectState result = null;
        MClass mclass = api.getSystem().model().getClass(className);
        for (MObject o : api.getSystem().state().allObjects()) {
            if (o.cls().allSupertypes().contains(mclass)) {
                result = o.state(api.getSystem().state());
                break;
            }
        }
        return result;
    }

    public static MAttribute getAttribute(UseSystemApi api, String className, String attributeName) {
        MClass mclass = api.getSystem().model().getClass(className);
        return mclass.attribute(attributeName, true);
    }

}
