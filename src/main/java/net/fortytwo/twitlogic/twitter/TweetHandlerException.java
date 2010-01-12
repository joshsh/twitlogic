package net.fortytwo.twitlogic.twitter;

/**
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:50:00 AM
 */
public class TweetHandlerException extends Exception {
    public TweetHandlerException(final Throwable cause) {
        super(cause);
    }

    public TweetHandlerException(final String msg) {
        super(msg);
    }

    public TweetHandlerException(final String msg,
                                 final Throwable cause) {
        super(msg, cause);
    }
}
