package net.fortytwo.twitlogic.syntax.afterthought.impl.miscellaneous;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.MiscellaneousVocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class MentionedBy extends ObjectPropertyAfterthoughtMatcher {
    protected String getPropertyURI() {
        return MiscellaneousVocabs.DATAGOVWIKI_NAMESPACE + "mentionedBy";
    }

    protected boolean predicateMatches(final String predicate) {
        String p = predicate.toLowerCase();
        return p.equals("mentioned by");
    }
}