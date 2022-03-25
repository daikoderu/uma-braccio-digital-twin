package digital.twin.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AttributeSpecification {

    private final Map<String, AttributeType> types;
    private final Map<String, Integer> multiplicities;

    public AttributeSpecification() {
        types = new HashMap<>();
        multiplicities = new HashMap<>();
    }

    public Set<String> attributeNames() {
        return types.keySet();
    }
    public AttributeType typeOf(String attributeName) {
        return types.get(attributeName);
    }
    public int multiplicityOf(String attributeName) {
        return multiplicities.get(attributeName);
    }
    public void set(String name, AttributeType type, int multiplicity) {
        if (multiplicity <= 0) {
            throw new IllegalArgumentException("multiplicity must be a positive integer");
        }
        types.put(name, Objects.requireNonNull(type));
        multiplicities.put(name, multiplicity);
    }
    public void set(String name, AttributeType type) {
        set(name, type, 1);
    }

}
