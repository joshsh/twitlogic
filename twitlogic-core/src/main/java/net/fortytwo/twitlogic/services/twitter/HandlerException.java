package net.fortytwo.twitlogic.services.twitter;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class HandlerException extends Exception {
    public HandlerException(final Throwable cause) {
        super(cause);
    }

    public HandlerException(final String msg) {
        super(msg);
    }

    public HandlerException(final String msg,
                            final Throwable cause) {
        super(msg, cause);
    }
}
