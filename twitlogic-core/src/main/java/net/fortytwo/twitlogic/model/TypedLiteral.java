package net.fortytwo.twitlogic.model;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TypedLiteral implements Resource {
    private final String label;
    private final String datatype;

    public TypedLiteral(final String label,
                        final String datatype) {
        this.label = label;
        this.datatype = datatype;
    }

    public String getLabel() {
        return label;
    }

    public String getDatatype() {
        return datatype;
    }

    public String toString() {
        return label;
    }

    public Type getType() {
        return Type.TYPED_LITERAL;
    }

    public boolean equals(final Object other) {
        return other instanceof TypedLiteral
                && datatype.equals(((TypedLiteral) other).datatype)
                && label.equals(((TypedLiteral) other).label);
    }

    public int hashCode() {
        return label.hashCode() + datatype.hashCode();
    }
}
