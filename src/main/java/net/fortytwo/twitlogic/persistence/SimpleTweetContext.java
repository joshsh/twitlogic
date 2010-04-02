package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.Resource;

/**
 * User: josh
 * Date: Apr 1, 2010
 * Time: 7:29:59 PM
 */
public class SimpleTweetContext implements TweetContext {
    private final Tweet tweet;

    public SimpleTweetContext(Tweet tweet) {
        this.tweet = tweet;
    }

    public User thisUser() {
        return tweet.getUser();
    }

    public Person thisPerson() {
        return thisUser().getHeldBy();
    }

    public User repliedToUser() {
        // TODO
        return null;
    }

    public User retweetedUser() {
        // TODO
        return null;
    }

    public Tweet thisTweet() {
        return tweet;
    }

    public Tweet repliedToTweet() {
        // TODO
        return null;
    }

    public Resource anonymousNode() {
        // TODO
        return null;
    }
}
