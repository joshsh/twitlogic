package net.fortytwo.twitlogic.services.twitter;

import org.apache.http.conn.HttpHostConnectException;

/**
 * User: josh
 * Date: Apr 13, 2010
 * Time: 2:09:10 PM
 */
public class TwitterConnectionRefusedException extends TwitterClientException {
    public TwitterConnectionRefusedException(final HttpHostConnectException e) {
        super(e);
    }
}
