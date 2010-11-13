package net.fortytwo.twitlogic.flow;

/**
 * User: josh
 * Date: Jun 29, 2010
 * Time: 5:26:05 PM
 */
public class Filter<T, E extends Exception> implements Handler<T, E> {
    private final Handler<T, E> baseHandler;
    private final Criterion<T> criterion;

    public Filter(final Criterion<T> criterion,
                  final Handler<T, E> baseHandler) {
        this.criterion = criterion;
        this.baseHandler = baseHandler;
    }

    public boolean handle(T t) throws E {
        return !criterion.allow(t) || baseHandler.handle(t);
    }
        
    public interface Criterion<S> {
        boolean allow(S s);
    }
}
