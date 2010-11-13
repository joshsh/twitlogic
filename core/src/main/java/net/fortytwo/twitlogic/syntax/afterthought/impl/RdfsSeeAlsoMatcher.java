package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.RDFS;

/**
 * User: josh
 * Date: Sep 29, 2009
 * Time: 10:29:09 PM
 */
public class RdfsSeeAlsoMatcher extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return RDFS.SEEALSO;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("see");
    }
}