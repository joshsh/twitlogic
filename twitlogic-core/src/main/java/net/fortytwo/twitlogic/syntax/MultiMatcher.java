package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.TweetContext;

/**
 * User: josh
 * Date: Oct 1, 2009
 * Time: 11:28:31 PM
 */
public class MultiMatcher implements Matcher {
    private final Matcher[] componentMatchers;

    public MultiMatcher(final Matcher... componentMatchers) {
        this.componentMatchers = componentMatchers;
    }

    public void match(final String expression,
                      final Handler<Triple> handler,
                      final TweetContext context) throws MatcherException {
        for (Matcher m : componentMatchers) {
            m.match(expression, handler, context);
        }
    }
}
