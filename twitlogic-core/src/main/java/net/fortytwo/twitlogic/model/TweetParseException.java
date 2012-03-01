package net.fortytwo.twitlogic.model;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TweetParseException extends Exception {
    public TweetParseException(final String msg) {
        super(msg);
    }

    public TweetParseException(final Throwable cause) {
        super(cause);
    }

    public TweetParseException(final String msg,
                               final Throwable cause) {
        super(msg, cause);
    }
}
