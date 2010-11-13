package net.fortytwo.twitlogic.persistence;

/**
 * User: josh
 * Date: Nov 30, 2009
 * Time: 6:02:43 PM
 */
public class TweetStoreException extends Exception {
    public TweetStoreException(final Throwable cause) {
        super(cause);
    }

    public TweetStoreException(final String message) {
        super(message);
    }
}
