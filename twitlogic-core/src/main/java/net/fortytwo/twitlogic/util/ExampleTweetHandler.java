package net.fortytwo.twitlogic.util;

import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.HandlerException;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: josh
 * Date: Sep 8, 2009
 * Time: 10:37:05 PM
 */
public class ExampleTweetHandler implements Handler<Tweet> {
//    private final Matcher matcher;

    public ExampleTweetHandler() {
  //      matcher = new MultiMatcher(new TwipleMatcher(),
    //            new DemoAfterthoughtMatcher());
    }

    public boolean handle(final Tweet tweet) throws HandlerException {
        System.out.println("" + tweet.getUser().getScreenName()
                + " [" + tweet.getId() + "]"
                + ": " + tweet.getText());

        TweetContext tweetContext = new TweetContext() {
            public User thisUser() {
                throw new IllegalStateException("not implemented");
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
                throw new IllegalStateException("not implemented");
            }

            public Tweet repliedToTweet() {
                throw new IllegalStateException("not implemented");
            }

            public Resource anonymousNode() {
                throw new IllegalStateException("not implemented");
            }
        };
        final List<Triple> results = new LinkedList<Triple>();
        Handler<Triple> handler = new Handler<Triple>() {
            public boolean handle(final Triple triple) throws HandlerException {
                results.add(triple);
                return true;
            }
        };

 /*       try {
            matcher.match(tweet.getText(), handler, tweetContext);
        } catch (MatcherException e) {
            throw new HandlerException(e);
        }
  */
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
