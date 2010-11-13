package net.fortytwo.twitlogic.flow;

/**
 * User: josh
 * Date: Oct 2, 2009
 * Time: 10:43:17 PM
 */
public class Splitter<T, E extends Exception> implements Handler<T, E> {
    private final Handler<T, E>[] handlers;

    public Splitter(final Handler<T, E>[] handlers) {
        this.handlers = handlers;
    }

    public boolean handle(final T t) throws E {
        for (Handler<T, E> handler : handlers) {
            // halt if any child handler halts
            if (!handler.handle(t)) {
                return false;
            }
        }

        return true;
    }
}
