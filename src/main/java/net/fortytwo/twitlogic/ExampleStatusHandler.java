package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.syntax.MatcherException;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
import net.fortytwo.twitlogic.syntax.twiple.TwipleMatcher;
import net.fortytwo.twitlogic.twitter.TwitterStatus;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: josh
 * Date: Sep 8, 2009
 * Time: 10:37:05 PM
 */
class ExampleStatusHandler implements Handler<TwitterStatus, Exception> {
    private final Matcher matcher;

    public ExampleStatusHandler() {
        matcher = new MultiMatcher(new TwipleMatcher(),
                new DemoAfterthoughtMatcher());
    }

    public boolean handle(final TwitterStatus status) throws Exception {
        System.out.println("" + status.getUser().getScreenName() + ": " + status.getText());

        TweetContext tweetContext = new TweetContext() {
            public User thisUser() {
                return null;
            }

            public Resource thisTweet() {
                return null;
            }
        };
        final List<Triple> results = new LinkedList<Triple>();
        Handler<Triple, MatcherException> handler = new Handler<Triple, MatcherException>() {
            public boolean handle(final Triple triple) throws MatcherException {
                results.add(triple);
                return true;
            }
        };

        matcher.match(status.getText(), handler, tweetContext);

        Comparator<Triple> cmp = new Comparator<Triple>() {
            public int compare(final Triple first,
                               final Triple second) {
                return ((Float) second.getWeight()).compareTo(first.getWeight());
            }
        };

        // Sort in order of decreasing weight.
        Collections.sort(results, cmp);

        if (0 < results.size()) {
            for (Triple t : results) {
                System.out.println("\t (" + t.getWeight() + ")\t" + t);
            }
        }

        return true;
    }
}
