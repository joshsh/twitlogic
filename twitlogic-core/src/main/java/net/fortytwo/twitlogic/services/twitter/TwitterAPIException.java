package net.fortytwo.twitlogic.services.twitter;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TwitterAPIException extends TwitterClientException {
    public TwitterAPIException() {
        super();
    }

    public TwitterAPIException(final String msg) {
        super(msg);
    }
}
