package net.fortytwo.twitlogic.flow;

import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class NullHandler<T> implements Handler<T> {
    public boolean isOpen() {
        return true;
    }

    public void handle(T t) throws HandlerException {
        // Do nothing.
    }
}
