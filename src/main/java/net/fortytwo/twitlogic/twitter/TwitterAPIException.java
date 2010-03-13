package net.fortytwo.twitlogic.twitter;

/**
 * User: josh
 * Date: Mar 12, 2010
 * Time: 8:32:12 PM
 */
public class TwitterAPIException extends TwitterClientException {
    public TwitterAPIException() {
        super();
    }

    public TwitterAPIException(final String msg) {
        super(msg);
    }
}
