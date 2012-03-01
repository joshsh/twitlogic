package net.fortytwo.twitlogic.syntax.afterthought.impl.foaf;

import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtContext;
import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtMatcher;
import net.fortytwo.twitlogic.syntax.MatcherException;
import net.fortytwo.twitlogic.vocabs.FOAF;

/**
 * Captures "+1" expression of own interest in a topic.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class SelfInterestMatcher extends AfterthoughtMatcher {
    public void matchNormalized(final String normed,
                                final AfterthoughtContext context) throws MatcherException {
        if (normed.equals("+1")) {
            Triple t = new Triple(
                    context.thisPerson(),
                    new URIReference(FOAF.INTEREST),
                    context.getSubject());

            try {
                context.handle(t);
            } catch (HandlerException e) {
                throw new MatcherException(e);
            }
        }
    }
}
