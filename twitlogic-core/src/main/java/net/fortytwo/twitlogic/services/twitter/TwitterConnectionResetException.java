package net.fortytwo.twitlogic.services.twitter;

import java.net.SocketException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TwitterConnectionResetException extends TwitterClientException {
    public TwitterConnectionResetException(final SocketException e) {
        super(e);
    }
}