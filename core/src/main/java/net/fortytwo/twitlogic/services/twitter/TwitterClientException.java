package net.fortytwo.twitlogic.services.twitter;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 10:06:32 PM
 */
public class TwitterClientException extends Exception {
    public TwitterClientException() {
        super();
    }

    public TwitterClientException(final Throwable cause) {
        super(cause);
    }

    public TwitterClientException(final String message) {
        super(message);
    }

    public TwitterClientException(final String message,
                                  final Throwable cause) {
        super(message, cause);
    }
}
