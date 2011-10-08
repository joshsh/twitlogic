package net.fortytwo.twitlogic.services.twitter;

import org.apache.http.conn.HttpHostConnectException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TwitterConnectionRefusedException extends TwitterClientException {
    public TwitterConnectionRefusedException(final HttpHostConnectException e) {
        super(e);
    }
}
