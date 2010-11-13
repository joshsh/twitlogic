package net.fortytwo.twitlogic.syntax.afterthought.impl.foaf;

import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 29, 2009
 * Time: 10:29:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class FoafMakerMatcher extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return FOAF.MAKER;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("maker")
                | p.equals("creator")
                | p.equals("made by")
                | p.equals("created by");
    }
}