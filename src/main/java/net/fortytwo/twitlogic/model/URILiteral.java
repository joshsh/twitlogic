package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.model.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 6, 2009
 * Time: 6:29:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class URILiteral implements Resource {
    private final String uri;

    public URILiteral(final String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public String toString() {
        return uri;
    }

    public Type getType() {
        return Type.URL;
    }
}
