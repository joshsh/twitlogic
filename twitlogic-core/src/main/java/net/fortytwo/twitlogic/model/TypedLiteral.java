package net.fortytwo.twitlogic.model;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 6, 2009
 * Time: 6:29:28 PM
 * To change this template use File | Settings | File Templates.
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
