package net.fortytwo.twitlogic.flow;

import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * An object which consumes data of a certain type.
 * A Handler may close at any time: you should call <code>isOpen</code> before each call to <code>handle</code>
 * to make sure the handler can accept new input.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface Handler<T> {
    /**
     * @return whether this Handler is open for new input
     */
    boolean isOpen();

    /**
     * Consumes a piece of data and performs some operation on that data.
     *
     * @throws HandlerException if any error occurs while processing the data
     */
    void handle(T t) throws HandlerException;
}
