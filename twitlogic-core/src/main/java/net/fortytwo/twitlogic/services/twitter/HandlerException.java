package net.fortytwo.twitlogic.services.twitter;

/**
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:50:00 AM
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
