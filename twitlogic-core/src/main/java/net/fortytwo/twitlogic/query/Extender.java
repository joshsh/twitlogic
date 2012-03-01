package net.fortytwo.twitlogic.query;

import java.util.Collection;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface Extender<T, M> {
    Collection<Memo<T, M>> findAlternatives(T t);
}
