package net.fortytwo.twitlogic.util;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * A convenience object which can be stacked on top of another <code>Handler</code>,
 * allowing input to that handler to be stopped.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class StopabbleHandler<T> implements Handler<T> {
    private final Handler<T> baseHandler;
    private boolean stopped = false;

    public StopabbleHandler(Handler<T> baseHandler) {
        this.baseHandler = baseHandler;
    }

    public boolean handle(T t) throws HandlerException {
        return !stopped && baseHandler.handle(t);
    }

    public void stop() {
        stopped = true;
    }

    public void reset() {
        stopped = false;
    }
}
