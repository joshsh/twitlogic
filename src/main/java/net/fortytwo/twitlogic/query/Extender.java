package net.fortytwo.twitlogic.query;

import java.util.Collection;

/**
 * User: josh
* Date: Oct 4, 2009
* Time: 3:15:28 PM
*/
public interface Extender<T, M> {
    Collection<Memo<T, M>> findAlternatives(T t);
}
