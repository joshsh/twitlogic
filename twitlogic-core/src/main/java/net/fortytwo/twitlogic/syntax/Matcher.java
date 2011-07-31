package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.model.Triple;

/**
 * User: josh
 * Date: Oct 1, 2009
 * Time: 8:30:00 PM
 */
public interface Matcher {
    void match(String expression,
               Handler<Triple> handler,
               TweetContext context) throws MatcherException;

}
