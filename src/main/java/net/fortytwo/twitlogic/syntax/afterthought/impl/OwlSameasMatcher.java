package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.OWL;

/**
 * User: josh
 * Date: Oct 26, 2009
 * Time: 6:39:56 PM
 */
public class OwlSameasMatcher  extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return OWL.SAMEAS;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("same as")
                | p.equals("=")
                | p.equals("ie")
                | p.equals("i.e.");
    }
}
