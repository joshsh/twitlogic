package net.fortytwo.twitlogic.model;

/**
 * User: josh
 * Date: Oct 2, 2009
 * Time: 3:01:11 AM
 */
public class Tweet implements Resource {
    private final String id;

    public Tweet(final String id) {
        this.id = id;
    }

    public Type getType() {
        return Type.TWEET;
    }

    public String toString() {
        return "[tweet #" + id + "]";
    }

    public String getId() {
        return id;
    }

    public boolean equals(final Object other) {
        return other instanceof Tweet
                && id.equals(((Tweet) other).id);
    }

    public int hashCode() {
        return id.hashCode();
    }
}
