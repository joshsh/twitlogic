package net.fortytwo.twitlogic.syntax.afterthought.impl.skos;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.SKOS;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class SkosBroaderMatcher extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return SKOS.BROADERTRANSITIVE;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("broader")
                //| p.equals("=>");
                | p.equals("=&gt;");
    }
}
