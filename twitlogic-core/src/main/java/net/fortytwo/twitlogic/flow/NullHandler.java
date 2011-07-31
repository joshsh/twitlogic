package net.fortytwo.twitlogic.flow;

import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * User: josh
 * Date: Nov 23, 2009
 * Time: 9:48:38 PM
 */
public class NullHandler<T> implements Handler<T> {
    public boolean handle(T t) throws HandlerException {
        // Do nothing.
        return true;
    }
}
