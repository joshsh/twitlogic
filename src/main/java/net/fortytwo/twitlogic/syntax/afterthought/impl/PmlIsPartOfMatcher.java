package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.PML2Relation;

/**
 * User: josh
 * Date: Oct 27, 2009
 * Time: 10:33:25 AM
 */
public class PmlIsPartOfMatcher extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return PML2Relation.ISPARTOF;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("part of");
    }
}
