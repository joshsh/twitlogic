package net.fortytwo.twitlogic.model;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class URIReference implements Resource {
    private final String value;

    public Type getType() {
        return Type.URI_REFERENCE;
    }

    public URIReference(final String value) {
        this.value = value;
    }

    public String toString() {
        return "<" + value + ">";
    }

    public String getValue() {
        return value;
    }

    public boolean equals(final Object other) {
        return other instanceof URIReference
                && value.equals(((URIReference) other).value);
    }

    public int hashCode() {
        return value.hashCode();
    }
}
