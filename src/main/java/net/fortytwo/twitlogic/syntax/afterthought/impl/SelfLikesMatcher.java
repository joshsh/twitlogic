package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtContext;
import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtMatcher;

/**
 * Captures "+1" expression of own interest in a topic.
 *
 * User: josh
 * Date: Oct 1, 2009
 * Time: 12:41:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class SelfLikesMatcher extends AfterthoughtMatcher {
    public void matchNormalized(final String normed,
                                final AfterthoughtContext context) {
        if (normed.equals("+1")) {
            // TODO: produce triples

        }
    }
}
