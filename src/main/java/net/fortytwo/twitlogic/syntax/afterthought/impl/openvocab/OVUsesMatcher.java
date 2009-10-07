package net.fortytwo.twitlogic.syntax.afterthought.impl.openvocab;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.OpenVocab;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 29, 2009
 * Time: 10:29:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class OVUsesMatcher extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return OpenVocab.USES;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("uses")
                | p.equals("use");
    }
}