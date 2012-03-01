package net.fortytwo.twitlogic.model;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
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