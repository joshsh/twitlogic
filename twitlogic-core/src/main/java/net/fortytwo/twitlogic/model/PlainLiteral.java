package net.fortytwo.twitlogic.model;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class PlainLiteral implements Resource {
    private final String label;

    public PlainLiteral(final String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return "\"" + label + "\"";
    }

    public Type getType() {
        return Type.PLAIN_LITERAL;
    }

    public boolean equals(final Object other) {
        return other instanceof PlainLiteral
                && label.equals(((PlainLiteral) other).label);
    }

    public int hashCode() {
        return label.hashCode();
    }

}
