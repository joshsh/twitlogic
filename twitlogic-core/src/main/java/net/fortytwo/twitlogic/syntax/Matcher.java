package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.model.Triple;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface Matcher {
    void match(String expression,
               Handler<Triple> handler,
               TweetContext context) throws MatcherException;

}
