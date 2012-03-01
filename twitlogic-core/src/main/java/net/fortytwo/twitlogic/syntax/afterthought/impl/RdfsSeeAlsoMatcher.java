package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.RDFS;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
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