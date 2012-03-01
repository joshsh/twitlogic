package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.services.twitter.CustomTwitterClient;
import net.fortytwo.twitlogic.services.twitter.TwitterClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: make it persistent
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class UserRegistry {
    private final Map<String, User> usersByScreenName;
    private final CustomTwitterClient client;

    public UserRegistry(final CustomTwitterClient client) {
        this.client = client;
        usersByScreenName = new HashMap<String, User>();
    }

    /**
     * @param user must contain at least user id and screen name
     */
    public void add(final User user) {
        if (null == user.getId() || null == user.getScreenName()) {
            throw new IllegalArgumentException("null id or screen name");
        }

        if (null == usersByScreenName.get(user.getScreenName())) {
            usersByScreenName.put(user.getScreenName(), user);
        }
    }

    public Integer resolveUserId(final String screenName) throws TwitterClientException {
        User user = usersByScreenName.get(screenName);

        if (null == user) {
            user = client.findUserInfo(screenName);
            add(user);
        }

        return user.getId();
    }

    public Handler<Tweet> createUserRegistryFilter(
            final Handler<Tweet> baseHandler) {
        return new Handler<Tweet>() {
            public boolean handle(final Tweet tweet) throws HandlerException {
                add(tweet.getUser());
                return baseHandler.handle(tweet);
            }
        };
    }

    public User findUserInfo(final String screenName) throws TwitterClientException {
        User user = client.findUserInfo(screenName);
        add(user);
        return user;
    }
}
