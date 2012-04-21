package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.syntax.MatcherException;

import java.util.Collection;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class CompoundAfterthoughtMatcher extends AfterthoughtMatcher {
    private final Collection<AfterthoughtMatcher> parsers;

    public CompoundAfterthoughtMatcher(Collection<AfterthoughtMatcher> parsers) {
        this.parsers = parsers;
    }

    private class BooleanWrapper {
        public boolean value;
    }

    protected void matchNormalized(final String normed,
                                   final AfterthoughtContext context) throws MatcherException {
        final BooleanWrapper matched = new BooleanWrapper();
        matched.value = false;

        Handler<Triple> singleMatchHandler = new Handler<Triple>() {
            public boolean isOpen() {
                return true;
            }

            public void handle(final Triple triple) throws HandlerException {
                context.handle(triple);
                matched.value = true;
            }
        };

        AfterthoughtContext newContext = new AfterthoughtContext(
                context.getSubject(),
                singleMatchHandler,
                context);

        for (AfterthoughtMatcher parser : parsers) {
            parser.matchNormalized(normed, newContext);
            if (matched.value) {
                return;
            }
        }
    }
}
