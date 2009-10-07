package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.twitter.TwitterClientException;

/**
 * User: josh
 * Date: Oct 5, 2009
 * Time: 2:07:49 AM
 */
public class PersistenceContext {
    private final UserRegistry userRegistry;

    public PersistenceContext(final UserRegistry userRegistry) {
        this.userRegistry = userRegistry;
    }

    public String valueOf(final Hashtag hashtag) {
        // TODO: assumes normalized hash tags
        return TwitLogic.HASHTAGS_BASEURI + hashtag.getName();
    }

    public String valueOf(final Tweet tweet) {
        return TwitLogic.TWEETS_BASEURI + tweet.getId();
    }

    public String valueOf(final User user) throws TwitterClientException {
        Integer id = user.getId();
        if (null == id) {
            id = userRegistry.resolveUserId(user.getScreenName());
        }

        return TwitLogic.USERS_BASEURI + id;
    }

    public UserRegistry getUserRegistry() {
        return userRegistry;
    }
}
