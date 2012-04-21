package net.fortytwo.twitlogic.services.twitter;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.logging.TweetStatistics;
import net.fortytwo.twitlogic.model.Place;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface TwitterClient {
    void processFollowers(User user,
                          Handler<User> handler) throws TwitterClientException, HandlerException;

    void processTimelineFrom(Set<User> users,
                             Date minTimestamp,
                             Date maxTimestamp,
                             Handler<Tweet> handler) throws TwitterClientException, HandlerException;

    void handleTimelineFrom(User user,
                            Date minTimestamp,
                            Date maxTimestamp,
                            Handler<Tweet> handler) throws TwitterClientException, HandlerException;

    boolean handlePublicTimelinePage(User user,
                                     int page,
                                     Handler<Tweet> handler) throws TwitterClientException, HandlerException;

    List<User> getFollowedUsers(User user) throws TwitterClientException;

    List<User> getFollowers(User user) throws TwitterClientException;

    List<User> getListMembers(User user,
                              String listId) throws TwitterClientException;

    void addToList(final User user,
                   final String listId,
                   final String userId) throws TwitterClientException;

    User findUserInfo(final String screenName) throws TwitterClientException;

    void updateStatus(final Tweet tweet) throws TwitterClientException;

    void search(final String term,
                final Handler<Tweet> handler) throws TwitterClientException;

    void processFilterStream(Collection<User> users,
                             Collection<String> terms,
                             Handler<Tweet> addHandler,
                             Handler<Tweet> deleteHandler,
                             int previousStatusCount) throws TwitterClientException;

    void processSampleStream(final Handler<Tweet> addHandler,
                             final Handler<Tweet> deleteHandler) throws TwitterClientException;

    void requestUserTimeline(final User user,
                             final Handler<Tweet> handler) throws TwitterClientException;

    Place fetchPlace(final String id) throws TwitterClientException;

    TwitterAPILimits getLimits() throws TwitterClientException;

    TweetStatistics getStatistics();

    // Close any Twitter streams currently open.
    void stop();
}
