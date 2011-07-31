package net.fortytwo.twitlogic.flow;

import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * User: josh
 * Date: Oct 2, 2009
 * Time: 10:43:17 PM
 */
public class Splitter<T> implements Handler<T> {
    private final Handler<T>[] handlers;

    public Splitter(final Handler<T>[] handlers) {
        this.handlers = handlers;
    }

    public boolean handle(final T t) throws HandlerException {
        for (Handler<T> handler : handlers) {
            // halt if any child handler halts
            if (!handler.handle(t)) {
                return false;
            }
        }

        return true;
    }
}
