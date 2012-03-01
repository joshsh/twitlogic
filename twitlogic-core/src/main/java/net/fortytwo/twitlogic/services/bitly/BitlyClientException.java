package net.fortytwo.twitlogic.services.bitly;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class BitlyClientException extends Exception {
    public BitlyClientException(final Throwable cause) {
        super(cause);
    }

    public BitlyClientException(final String message) {
        super(message);
    }
}
