package net.fortytwo.twitlogic.syntax.afterthought.impl.miscellaneous;

import net.fortytwo.twitlogic.syntax.afterthought.ObjectPropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.MiscellaneousVocabs;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 29, 2009
 * Time: 10:29:09 PM
 * To change this template use File | Settings | File Templates.
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