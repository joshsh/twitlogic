package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.OWL;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class OwlSameasMatcher  extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return OWL.SAMEAS;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("same as")
                | p.equals("sameas")
                | p.equals("=")
                | p.equals("aka")
                | p.equals("a.k.a.")
                | p.equals("ie")
                | p.equals("i.e.");
    }
}
