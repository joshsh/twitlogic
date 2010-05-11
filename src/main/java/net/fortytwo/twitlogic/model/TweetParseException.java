package net.fortytwo.twitlogic.model;

/**
 * User: josh
 * Date: May 10, 2010
 * Time: 8:11:42 PM
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
