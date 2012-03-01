package net.fortytwo.twitlogic.syntax.afterthought.impl.foaf;

import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class FoafMadeMatcher extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return FOAF.MADE;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("made")
                | p.equals("make")
                | p.equals("makes")
                | p.equals("maker of")
                | p.equals("makers of")
                | p.equals("created")
                | p.equals("creator of")
                | p.equals("creators of");
    }
}