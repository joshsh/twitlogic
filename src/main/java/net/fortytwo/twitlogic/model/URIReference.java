package net.fortytwo.twitlogic.model;

/**
 * User: josh
 * Date: Sep 29, 2009
 * Time: 8:06:56 PM
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
