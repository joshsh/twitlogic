package net.fortytwo.twitlogic.flow;

import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * User: josh
 * Date: Jun 29, 2010
 * Time: 5:26:05 PM
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
