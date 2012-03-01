package net.fortytwo.twitlogic.syntax.afterthought.impl.openvocab;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.OpenVocab;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class OVStudiesMatcher extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return OpenVocab.STUDIES;
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("studies")
                | p.equals("study");
    }
}