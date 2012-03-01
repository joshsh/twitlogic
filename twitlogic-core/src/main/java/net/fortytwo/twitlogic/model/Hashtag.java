package net.fortytwo.twitlogic.model;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Hashtag implements Resource {
    private final String name;

    public Hashtag(final String name) {
        this.name = name.trim().toLowerCase();
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

    public boolean equals(final Object other) {
        return other instanceof Hashtag
                && name.equals(((Hashtag) other).name);
    }

    public int hashCode() {
        return name.hashCode();
    }
}
