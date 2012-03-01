package net.fortytwo.twitlogic.persistence;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TweetStoreException extends Exception {
    public TweetStoreException(final Throwable cause) {
        super(cause);
    }

    public TweetStoreException(final String message) {
        super(message);
    }
}
