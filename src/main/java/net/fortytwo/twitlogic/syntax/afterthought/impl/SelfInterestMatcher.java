package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtContext;
import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtMatcher;
import net.fortytwo.twitlogic.syntax.MatcherException;
import net.fortytwo.twitlogic.vocabs.FOAF;

/**
 * Captures "+1" expression of own interest in a topic.
 *
 * User: josh
 * Date: Oct 1, 2009
 * Time: 12:41:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class SelfInterestMatcher extends AfterthoughtMatcher {
    public void matchNormalized(final String normed,
                                final AfterthoughtContext context) throws MatcherException {
        if (normed.equals("+1")) {
            Triple t = new Triple(
                    context.thisPerson(),
                    new URIReference(FOAF.INTEREST),
                    context.getSubject());

            context.handle(t);
        }
    }
}
