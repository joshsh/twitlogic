package net.fortytwo.twitlogic.model;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 29, 2009
 * Time: 8:06:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class URIReference implements Resource {
    private final String uri;

    public Type getType() {
        return Type.URI_REFERENCE;
    }

    public URIReference(final String uri) {
        this.uri = uri;
    }

    public String toString() {
        return "<" + uri + ">";
    }

    public boolean equals(final Object other) {
        return other instanceof URIReference
                && uri.equals(((URIReference) other).uri);
    }

    public int hashCode() {
        return uri.hashCode();
    }
}
