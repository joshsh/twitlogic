package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.PML2Relation;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class PmlIsPartOfMatcher extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return PML2Relation.ISPARTOF;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("part of")
                | p.equals("partof");
    }
}
