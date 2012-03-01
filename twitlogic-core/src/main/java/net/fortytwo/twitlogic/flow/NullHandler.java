package net.fortytwo.twitlogic.flow;

import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class NullHandler<T> implements Handler<T> {
    public boolean handle(T t) throws HandlerException {
        // Do nothing.
        return true;
    }
}
