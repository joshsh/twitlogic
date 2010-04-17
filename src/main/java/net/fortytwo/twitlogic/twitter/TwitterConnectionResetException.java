package net.fortytwo.twitlogic.twitter;

import java.net.SocketException;

/**
 * User: josh
 * Date: Apr 13, 2010
 * Time: 2:09:10 PM
 */
public class TwitterConnectionResetException extends TwitterClientException {
    public TwitterConnectionResetException(final SocketException e) {
        super(e);
    }
}