package net.fortytwo.twitlogic.model;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 5, 2009
 * Time: 1:26:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class Dollartag implements Resource {
    private final String name;

    public Dollartag(final String name) {
        this.name = name.trim().toUpperCase();
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "$" + name;
    }

    public Type getType() {
        return Type.DOLLARTAG;
    }

    public boolean equals(final Object other) {
        return other instanceof Dollartag
                && name.equals(((Dollartag) other).name);
    }

    public int hashCode() {
        return name.hashCode();
    }
}