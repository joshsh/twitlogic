package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.syntax.MatcherException;
import net.fortytwo.twitlogic.syntax.TweetSyntax;

import java.util.regex.Pattern;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class ObjectPropertyAfterthoughtMatcher extends AfterthoughtMatcher {

    protected abstract String getPropertyURI();

    protected abstract boolean predicateMatches(final String predicate);

    public void matchNormalized(final String normed,
                                final AfterthoughtContext context) throws MatcherException {
        TokenizedObjectPropertyClause c = forResourceObject(normed);

        if (null == c) {
            return;
        }

        String predicate = c.getBeforeObject();
        Resource object = c.getObject();
        // Note: anything after the object is ignored

        if (predicateMatches(predicate)) {
            try {
                context.handleCompletedTriple(new URIReference(getPropertyURI()), object);
            } catch (HandlerException e) {
                throw new MatcherException(e);
            }
        }
    }

    protected TokenizedObjectPropertyClause forResourceObject(final String normed) {
        TokenizedObjectPropertyClause c;
        c = forHashtagObject(normed);
        if (null == c) {
            c = forUsernameObject(normed);
            if (null == c) {
                c = forUrlObject(normed);
            }
        }

        return c;
    }

    private TokenizedObjectPropertyClause forHashtagObject(final String normed) {
        ThreeParts t = divide(normed, TweetSyntax.HASHTAG_PATTERN);
        return null == t
                ? null
                : new TokenizedObjectPropertyClause(t.first, new Hashtag(t.second.substring(1)), t.third);
    }

    private TokenizedObjectPropertyClause forUsernameObject(final String normed) {
        ThreeParts t = divide(normed, TweetSyntax.USERNAME_PATTERN);
        return null == t
                ? null
                : new TokenizedObjectPropertyClause(t.first, new User(t.second.substring(1)).getHeldBy(), t.third);
    }

    // TODO: redirection resolution
    private TokenizedObjectPropertyClause forUrlObject(final String normed) {
        ThreeParts t = divide(normed, TweetSyntax.URL_PATTERN);
        return null == t
                ? null
                : new TokenizedObjectPropertyClause(t.first, new URIReference(t.second), t.third);
    }

    private ThreeParts divide(final String whole,
                              final Pattern pattern) {
        String[] parts = pattern.split(whole + " ");
        if (2 == parts.length) {
            String first = parts[0];
            String third = parts[1];

            String second = whole.substring(first.length(), 1 + whole.length() - third.length());
            ThreeParts t = new ThreeParts();
            t.first = first.trim();
            t.second = second.trim();
            t.third = third.trim();

            return t;
        }

        return null;
    }

    private class ThreeParts {
        public String first;
        public String second;
        public String third;
    }

    protected class TokenizedObjectPropertyClause {
        private final String beforeObject;
        private final Resource object;
        private final String afterObject;

        public TokenizedObjectPropertyClause(final String beforeObject,
                                             final Resource object,
                                             final String afterObject) {
            this.beforeObject = beforeObject;
            this.object = object;
            this.afterObject = afterObject;
        }

        public String getBeforeObject() {
            return beforeObject;
        }

        public Resource getObject() {
            return object;
        }

        public String getAfterObject() {
            return afterObject;
        }
    }
}
