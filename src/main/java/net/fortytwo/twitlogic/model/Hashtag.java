package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.model.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 5, 2009
 * Time: 1:26:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class Hashtag implements Resource {
    private final String name;

    public Hashtag(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "#" + name;
    }

    public Type getType() {
        return Type.HASHTAG;
    }
}
