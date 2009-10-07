package net.fortytwo.twitlogic.syntax;

import junit.framework.TestCase;
import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: josh
 * Date: Oct 1, 2009
 * Time: 9:12:46 PM
 */
public abstract class MatcherTestBase extends TestCase {
    protected Matcher matcher;
    protected final TweetContext tweetContext = new TweetContext() {
        public User thisUser() {
            return null;
        }

        public Person thisPerson() {
            return thisUser().getHeldBy();
        }

        public User repliedToUser() {
            return null;
        }

        public User retweetedUser() {
            return null;
        }

        public Tweet thisTweet() {
            return null;
        }

        public Tweet repliedToTweet() {
            return null;
        }

        public Resource anonymousNode() {
            return null;
        }
    };
    protected final Set<Triple> results = new HashSet<Triple>();
    protected final Handler<Triple, MatcherException> handler = new Handler<Triple, MatcherException>() {
        public boolean handle(final Triple triple) throws MatcherException {
            results.add(triple);
            return true;
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
}
