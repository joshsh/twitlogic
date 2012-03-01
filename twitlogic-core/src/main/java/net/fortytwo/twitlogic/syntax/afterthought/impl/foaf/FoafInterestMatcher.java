package net.fortytwo.twitlogic.syntax.afterthought.impl.foaf;

import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;

/**
 * Expression of third-party's interest in a topic.  Possibly intrusive (?).
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class FoafInterestMatcher extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return FOAF.INTEREST;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("likes")
                | p.equals("like");
    }
}