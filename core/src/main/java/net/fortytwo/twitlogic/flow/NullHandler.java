package net.fortytwo.twitlogic.flow;

/**
 * User: josh
 * Date: Nov 23, 2009
 * Time: 9:48:38 PM
 */
public class NullHandler<T, E extends Exception> implements Handler<T, E> {
    public boolean handle(T t) throws E {
        // Do nothing.
        return true;
    }
}
