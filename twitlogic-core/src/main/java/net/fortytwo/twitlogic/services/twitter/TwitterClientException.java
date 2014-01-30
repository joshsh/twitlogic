package net.fortytwo.twitlogic.services.twitter;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
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

    public static class UnauthorizedException extends TwitterClientException {
        public UnauthorizedException(final Throwable cause) {
            super(cause);
        }
    }
}
