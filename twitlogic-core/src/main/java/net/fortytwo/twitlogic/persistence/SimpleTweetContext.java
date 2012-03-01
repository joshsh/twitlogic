package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.model.User;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class SimpleTweetContext implements TweetContext {
    private final Tweet tweet;

    public SimpleTweetContext(final Tweet tweet) {
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
        throw new IllegalStateException("not implemented");
    }

    public User retweetedUser() {
        // TODO
        throw new IllegalStateException("not implemented");
    }

    public Tweet thisTweet() {
        return tweet;
    }

    public Tweet repliedToTweet() {
        // TODO
        throw new IllegalStateException("not implemented");
    }

    public Resource anonymousNode() {
        return new URIReference(SesameTools.createRandomMiscellaneousURIString());
    }
}
