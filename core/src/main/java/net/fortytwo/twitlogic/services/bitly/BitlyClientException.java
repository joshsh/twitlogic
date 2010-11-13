package net.fortytwo.twitlogic.services.bitly;

/**
 * User: josh
 * Date: Oct 7, 2009
 * Time: 12:15:55 AM
 */
public class BitlyClientException extends Exception {
    public BitlyClientException(final Throwable cause) {
        super(cause);
    }

    public BitlyClientException(final String message) {
        super(message);
    }
}
