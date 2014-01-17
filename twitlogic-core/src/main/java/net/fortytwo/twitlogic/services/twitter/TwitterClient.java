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
    static final int MAX_SEARCH_COUNT = 100;

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

    List<User> getFollowees(User user) throws TwitterClientException;

    List<User> getFollowers(User user) throws TwitterClientException;

    List<User> getListMembers(User user,
                              String listId) throws TwitterClientException;

    void addToList(User user,
                   String listId,
                   String userId) throws TwitterClientException;

    User findUserInfo(String screenName) throws TwitterClientException;

    void updateStatus(Tweet tweet) throws TwitterClientException;

    void search(String term,
                GeoDisc geo,
                Handler<Tweet> handler) throws TwitterClientException, HandlerException;

    void processFilterStream(Collection<User> users,
                             Collection<String> terms,
                             double[][] locations,
                             Handler<Tweet> addHandler,
                             Handler<Tweet> deleteHandler,
                             int previousStatusCount) throws TwitterClientException;

    void processSampleStream(Handler<Tweet> addHandler,
                             Handler<Tweet> deleteHandler) throws TwitterClientException;

    void requestUserTimeline(User user,
                             Handler<Tweet> handler) throws TwitterClientException, HandlerException;

    Place fetchPlace(String id) throws TwitterClientException;

    TwitterAPILimits getLimits() throws TwitterClientException;

    TweetStatistics getStatistics();

    // Close any Twitter streams currently open.
    void stop();

    public class GeoDisc {
        private final double latitude;
        private final double longitude;
        private final double radius;

        public GeoDisc(double latitude, double longitude, double radius) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
        }

        public GeoDisc(final String geoStr) {
            String[] a = geoStr.split(",");
            if (3 != a.length) {
                throw new IllegalArgumentException("badly-formatted geo disk: " + geoStr);
            }

            try {
                latitude = Double.valueOf(a[0].trim());
                longitude = Double.valueOf(a[1].trim());
                radius = Double.valueOf(a[2].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("badly-formatted geo disk: " + geoStr);
            }

            if (latitude > 90 || latitude < -90 || longitude < -180 || longitude > 180 || radius < 0 || radius > 20038) {
                throw new IllegalArgumentException("badly-formatted geo disk: " + geoStr);
            }
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getRadius() {
            return radius;
        }
    }
}
