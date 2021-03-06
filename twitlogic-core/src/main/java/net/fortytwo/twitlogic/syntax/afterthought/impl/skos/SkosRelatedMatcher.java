package net.fortytwo.twitlogic.syntax.afterthought.impl.skos;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.SKOS;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class SkosRelatedMatcher  extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return SKOS.RELATED;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("related to");
    }
}
