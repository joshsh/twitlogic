package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 29, 2009
 * Time: 10:29:09 PM
 * To change this template use File | Settings | File Templates.
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
                | p.equals("creates")
                | p.equals("creator of")
                | p.equals("creators of");
    }
}