package digital.twin;

import java.util.*;

/**
 * @author Daniel Pérez - University of Málaga
 * Represents a specification of the attributes to store in the data lake. Each attribute has a type
 * and a fixed multiplicity.
 */
public class AttributeSpecification {

    private final Map<String, AttributeType> types;
    private final Map<String, Integer> multiplicities;

    /**
     * Default constructor.
     */
    public AttributeSpecification() {
        types = new HashMap<>();
        multiplicities = new HashMap<>();
    }

    /**
     * Returns a set containing the names of the attributes.
     * @return A read-only set containing the names of the attributes.
     */
    public Set<String> attributeNames() {
        return Collections.unmodifiableSet(types.keySet());
    }

    /**
     * Returns the type of the attribute <i>attributeName</i>.
     * @param attributeName The name of the attribute whose type to retrieve.
     * @return The type of the attribute, as an AttributeType.
     */
    public AttributeType typeOf(String attributeName) {
        return types.get(attributeName);
    }

    /**
     * Returns the multiplicity of the attribute <i>attributeName</i>.
     * @param attributeName The name of the attribute whose multiplicity to retrieve.
     * @return The multiplicity of the attribute.
     */
    public int multiplicityOf(String attributeName) {
        return multiplicities.get(attributeName);
    }

    /**
     * Adds or sets an attribute for this AttributeSpecification.
     * @param name The name of the attribute to add or set.
     * @param type The type of the attribute.
     * @param multiplicity The multiplicity of the attribute.
     */
    public void set(String name, AttributeType type, int multiplicity) {
        if (multiplicity <= 0) {
            throw new IllegalArgumentException("multiplicity must be a positive integer");
        }
        types.put(name, Objects.requireNonNull(type));
        multiplicities.put(name, multiplicity);
    }

    /**
     * Adds or sets an attribute for this AttributeSpecification with multiplicity 1.
     * @param name The name of the attribute to add or set.
     * @param type The type of the attribute.
     */
    public void set(String name, AttributeType type) {
        set(name, type, 1);
    }

}
