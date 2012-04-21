package net.fortytwo.twitlogic.syntax;

import junit.framework.TestCase;
import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.HandlerException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class MatcherTestBase extends TestCase {
    protected static final String BNODE_URI_BASE = "http://example.org/bnodes/";
    protected int bnodeIndex = 0;
    protected Tweet currentTweet = new Tweet();

    protected Matcher matcher;

    protected final TweetContext tweetContext = new TweetContext() {
        private final User user = new User(1234567890);

        public User thisUser() {
            return user;
        }

        public Person thisPerson() {
            return thisUser().getHeldBy();
        }

        public User repliedToUser() {
            throw new IllegalStateException("not implemented");
        }

        public User retweetedUser() {
            throw new IllegalStateException("not implemented");
        }

        public Tweet thisTweet() {
            return currentTweet;
        }

        public Tweet repliedToTweet() {
            throw new IllegalStateException("not implemented");
        }

        public Resource anonymousNode() {
            return bnode(++bnodeIndex);
        }
    };

    protected final Set<Triple> results = new HashSet<Triple>();

    protected final Handler<Triple> handler = new Handler<Triple>() {
        public boolean isOpen() {
            return true;
        }

        public void handle(final Triple triple) throws HandlerException {
            results.add(triple);
        }
    };

    protected void assertClausesEqual(final String expression1,
                                      final String expression2) throws MatcherException {
        results.clear();
        matcher.match(expression1, handler, tweetContext);
        Triple[] firstResults = new Triple[results.size()];
        results.toArray(firstResults);
        assertExpected(expression2, firstResults);
    }

    protected void assertExpected(final String expression,
                                  final Triple... expectedTriples) throws MatcherException {
        currentTweet.setText(expression);
        results.clear();
        matcher.match(expression, handler, tweetContext);
        Set<Triple> expected = new HashSet<Triple>();
        expected.addAll(Arrays.asList(expectedTriples));
        for (Triple t : expected) {
            if (!results.contains(t)) {
                fail("expected triple not found: " + t);
            }
        }
        for (Triple t : results) {
            if (!expected.contains(t)) {
                fail("unexpected triple found: " + t);
            }
        }
    }

    protected URIReference bnode(final int id) {
        return new URIReference(BNODE_URI_BASE + id);
    }
}
