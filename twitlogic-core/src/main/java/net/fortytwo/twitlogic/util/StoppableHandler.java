package net.fortytwo.twitlogic.util;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * A convenience object which can be stacked on top of another <code>Handler</code>,
 * allowing input to that handler to be stopped.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class StoppableHandler<T> implements Handler<T> {
    private final Handler<T> baseHandler;
    private boolean stopped = false;

    public StoppableHandler(Handler<T> baseHandler) {
        this.baseHandler = baseHandler;
    }

    public boolean isOpen() {
        return !stopped && baseHandler.isOpen();
    }

    public void handle(T t) throws HandlerException {
        baseHandler.handle(t);
    }

    public void stop() {
        stopped = true;
    }

    public void reset() {
        stopped = false;
    }
}
