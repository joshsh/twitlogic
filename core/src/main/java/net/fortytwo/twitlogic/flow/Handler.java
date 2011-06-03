package net.fortytwo.twitlogic.flow;

import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 3, 2009
 * Time: 10:03:05 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Handler<T> {
    boolean handle(T t) throws HandlerException;
}
