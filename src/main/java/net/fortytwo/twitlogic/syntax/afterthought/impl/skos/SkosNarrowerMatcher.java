package net.fortytwo.twitlogic.syntax.afterthought.impl.skos;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.SKOS;

/**
 * User: josh
 * Date: Oct 27, 2009
 * Time: 2:39:13 AM
 */
public class SkosNarrowerMatcher extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return SKOS.NARROWER;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("narrower")
//                | p.equals("<=");
                | p.equals("&lt;=");
    }
}
