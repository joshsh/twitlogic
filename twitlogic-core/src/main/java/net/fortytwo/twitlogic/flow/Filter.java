package net.fortytwo.twitlogic.flow;

import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Filter<T> implements Handler<T> {
    private final Handler<T> baseHandler;
    private final Criterion<T> criterion;

    public Filter(final Criterion<T> criterion,
                  final Handler<T> baseHandler) {
        this.criterion = criterion;
        this.baseHandler = baseHandler;
    }

    public boolean handle(T t) throws HandlerException {
        return !criterion.allow(t) || baseHandler.handle(t);
    }
        
    public interface Criterion<S> {
        boolean allow(S s);
    }
}
