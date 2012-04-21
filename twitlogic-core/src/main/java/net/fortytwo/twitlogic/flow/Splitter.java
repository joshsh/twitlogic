package net.fortytwo.twitlogic.flow;

import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Splitter<T> implements Handler<T> {
    private final Handler<T>[] handlers;

    public Splitter(final Handler<T>[] handlers) {
        this.handlers = handlers;
    }

    public boolean isOpen() {
        // TODO
        return true;
    }

    public void handle(final T t) throws HandlerException {
        for (Handler<T> handler : handlers) {
            handler.handle(t);
        }
    }
}
