package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;

/**
 * User: josh
 * Date: Sep 30, 2009
 * Time: 1:46:56 AM
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
