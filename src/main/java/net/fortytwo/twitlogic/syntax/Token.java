package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.model.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 6, 2009
 * Time: 6:59:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Token {
    public enum Type {
        HASHTAG,
        QUOTED_STRING,
        SCREEN_NAME,
        URL
    }

    private final Resource resource;
    private final Type type;

    public Token(final Resource resource,
                     final Type type) {
        this.resource = resource;
        this.type = type;
    }

    public Resource getResource() {
        return resource;
    }

    public Type getType() {
        return type;
    }
}
