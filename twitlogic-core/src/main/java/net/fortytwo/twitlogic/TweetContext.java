package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface TweetContext {
    User thisUser();

    Person thisPerson();

    User repliedToUser();

    User retweetedUser();

    Tweet thisTweet();

    Tweet repliedToTweet();

    Resource anonymousNode();
}
