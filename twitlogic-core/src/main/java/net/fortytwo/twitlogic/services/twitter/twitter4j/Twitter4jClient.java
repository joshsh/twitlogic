package net.fortytwo.twitlogic.services.twitter.twitter4j;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.logging.TweetStatistics;
import net.fortytwo.twitlogic.model.Place;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.services.twitter.TwitterAPILimits;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import net.fortytwo.twitlogic.services.twitter.TwitterClientException;
import net.fortytwo.twitlogic.services.twitter.TwitterCredentials;
import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.PagableResponseList;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterResponse;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserList;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Twitter4jClient implements TwitterClient {
    private static final Logger LOGGER = Logger.getLogger(Twitter4jClient.class.getName());

    private final Twitter twitter;
    private final TwitterStreamFactory streamFactory;
    private final TweetStatistics statistics = new TweetStatistics();

    public Twitter4jClient() throws TwitterClientException {
        TwitterCredentials cred = new TwitterCredentials();

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(cred.getConsumerKey())
                .setOAuthConsumerSecret(cred.getConsumerSecret())
                .setOAuthAccessToken(cred.getAccessToken())
                .setOAuthAccessTokenSecret(cred.getTokenSecret());

        Configuration conf = cb.build();

        TwitterFactory tf = new TwitterFactory(conf);
        twitter = tf.getInstance();

        streamFactory = new TwitterStreamFactory(conf);
    }

    public void processFollowers(User user, Handler<User> handler) throws TwitterClientException, HandlerException {
        //IDs ids = twitter.getFollowersIDs(user.getId());

        throw new UnsupportedOperationException("not yet implemented");
    }

    public void processTimelineFrom(Set<User> users, Date minTimestamp, Date maxTimestamp, Handler<Tweet> handler) throws TwitterClientException, HandlerException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void handleTimelineFrom(User user, Date minTimestamp, Date maxTimestamp, Handler<Tweet> handler) throws TwitterClientException, HandlerException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean handlePublicTimelinePage(User user, int page, Handler<Tweet> handler) throws TwitterClientException, HandlerException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public List<User> getFollowedUsers(User user) throws TwitterClientException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public List<User> getFollowers(User user) throws TwitterClientException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public List<User> getListMembers(final User user,
                                     final String listId) throws TwitterClientException {
        LOGGER.info("getting members of list " + listId + " owned by " + user);
        // TODO: this is inefficient
        Map<String, UserList> lists = new HashMap<String, UserList>();
        for (UserList l : getUserLists(user)) {
            lists.put(l.getName(), l);
        }
        final UserList list = lists.get(listId);
        if (null == list) {
            throw new TwitterClientException("no such list: " + listId + " owned by user " + user);
        }

        List<User> result = new LinkedList<User>();
        for (twitter4j.User u : asList(new ListGenerator<twitter4j.User>() {
            public PagableResponseList getList(long cursor) throws TwitterException {
                return twitter.getUserListMembers(list.getId(), cursor);
            }
        })) {
            result.add(new User(u));
        }

        for (User u : result) {
            LOGGER.info("\tfound: " + u);
        }
        return result;
    }

    private List<UserList> getUserLists(final User user) throws TwitterClientException {
        LOGGER.info("finding lists of user " + user);

        try {
            return twitter.getUserLists(user.getScreenName());
        } catch (TwitterException e) {
            throw new TwitterClientException(e);
        }

        /*
        return asList(new ListGenerator<UserList>() {
            public PagableResponseList getList(long cursor) throws TwitterException {
                twitter.getUserLists)
                return twitter.getUserLists(user.getScreenName(), cursor);
            }
        });*/
    }

    public void addToList(User user, String listId, String userId) throws TwitterClientException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public User findUserInfo(String screenName) throws TwitterClientException {
        try {
            return new User(twitter.showUser(screenName));
        } catch (TwitterException e) {
            throw new TwitterClientException(e);
        }
    }

    public void updateStatus(Tweet tweet) throws TwitterClientException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void search(final String term,
                       final GeoDisc geo,
                       final Handler<Tweet> handler) throws TwitterClientException, HandlerException {
        Query query = new Query(term);
        query.setCount(MAX_SEARCH_COUNT);

        if (null != geo) {
            GeoLocation loc = new GeoLocation(geo.getLatitude(), geo.getLongitude());
            query.setGeoCode(loc, geo.getRadius(), Query.KILOMETERS);
        }

        QueryResult result = null;

        do {
            query = null == result ? query : result.nextQuery();
            try {
                LOGGER.info("requesting search results");
                result = twitter.search(query);
            } catch (TwitterException e) {
                throw new TwitterClientException(e);
            }

            for (Status status : result.getTweets()) {
                if (!handler.isOpen()) {
                    LOGGER.info("handler closed");
                    return;
                }

                handler.handle(new Tweet(status));
            }
            if (result.hasNext()) {
                System.out.println("there are more results!");
            } else {
                System.out.println("no more results");
            }
        } while (result.hasNext());
    }

    public void processFilterStream(final Collection<User> users,
                                    final Collection<String> terms,
                                    final double[][] locations,
                                    final Handler<Tweet> addHandler,
                                    final Handler<Tweet> deleteHandler,
                                    final int previousStatusCount) throws TwitterClientException {
        if (previousStatusCount > 0) {
            throw new UnsupportedOperationException("gathering of historical tweets is not yet supported in the Twitter4j client");
        }

        int i;

        FilterQuery query = new FilterQuery();

        if (users.size() > 0) {
            long[] follow = new long[users.size()];
            i = 0;
            for (User u : users) {
                follow[i] = u.getId();
                i++;
            }
            query.follow(follow);
        }

        if (terms.size() > 0) {
            String[] track = new String[terms.size()];
            i = 0;
            for (String s : terms) {
                track[i] = s;
                i++;
            }
            query.track(track);
        }

        if (null != locations) {
            query.locations(locations);

            /*
            double [][] loc = {{ 51.280430, -0.563160 },{ 51.683979, 0.278970 }}; // london
            double[][] loc = {{49.871159, -6.379880}, {55.811741, 1.768960}}; // england

            query.locations(loc);
            */
        }

        TwitterStream stream = streamFactory.getInstance();
        stream.addListener(new InnerStatusHandler(stream, addHandler, deleteHandler));
        stream.filter(query);

        waitIndefinitely();
    }

    private final Object m = "";

    private void waitIndefinitely() throws TwitterClientException {
        synchronized (m) {
            try {
                m.wait();
            } catch (InterruptedException e) {
                throw new TwitterClientException(e);
            }
        }
    }

    public void processSampleStream(Handler<Tweet> addHandler, Handler<Tweet> deleteHandler) throws TwitterClientException {
        TwitterStream stream = streamFactory.getInstance();
        stream.addListener(new InnerStatusHandler(stream, addHandler, deleteHandler));
        stream.sample();

        waitIndefinitely();
    }

    public void requestUserTimeline(final User user,
                                    final Handler<Tweet> handler) throws TwitterClientException, HandlerException {
        List<Status> statuses;
        try {
            statuses = twitter.getUserTimeline(user.getScreenName());
        } catch (TwitterException e) {
            throw new TwitterClientException(e);
        }

        for (Status status : statuses) {
            if (!handler.isOpen()) {
                break;
            }

            handler.handle(new Tweet(status));
        }
    }

    public Place fetchPlace(String id) throws TwitterClientException {
        try {
            return new Place(twitter.getGeoDetails(id));
        } catch (TwitterException e) {
            throw new TwitterClientException(e);
        }
    }

    public TwitterAPILimits getLimits() throws TwitterClientException {
        try {
            return new Twitter4jLimits(twitter.getRateLimitStatus());
        } catch (TwitterException e) {
            throw new TwitterClientException(e);
        }
    }

    public TweetStatistics getStatistics() {
        return statistics;
    }

    public void stop() {
        twitter.shutdown();
    }

    private static class InnerStatusHandler implements StatusListener {
        private final TwitterStream stream;
        private final Handler<Tweet> addHandler;
        private final Handler<Tweet> deleteHandler;

        public InnerStatusHandler(final TwitterStream stream,
                                  final Handler<Tweet> addHandler,
                                  final Handler<Tweet> deleteHandler) {
            this.stream = stream;
            this.addHandler = addHandler;
            this.deleteHandler = deleteHandler;
        }

        public void onStatus(Status status) {
            if (null == addHandler) {
                return;
            }

            if (!addHandler.isOpen()) {
                stream.shutdown();
                return;
            }

            try {
                addHandler.handle(new Tweet(status));
            } catch (HandlerException e) {
                LOGGER.severe("exception in tweet handling: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }

        public void onDeletionNotice(StatusDeletionNotice notice) {
            if (null == deleteHandler) {
                return;
            }

            if (!deleteHandler.isOpen()) {
                stream.shutdown();
                return;
            }

            Tweet d = new Tweet("" + notice.getStatusId());
            try {
                deleteHandler.handle(d);
            } catch (HandlerException e) {
                LOGGER.severe("exception in tweet deletion: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }

        public void onTrackLimitationNotice(int i) {
            LOGGER.warning("tracking limitation notices are currently ignored");
        }

        public void onScrubGeo(long l, long l1) {
            LOGGER.warning("geo-scrubbing is not yet supported");
        }

        public void onStallWarning(StallWarning stallWarning) {
            LOGGER.warning("stall warning; Twitter client may be disconnected");
        }

        public void onException(Exception e) {
            LOGGER.severe("exception in tweet retrieval: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }


    private static <T extends TwitterResponse> List<T> asList(ListGenerator<T> g) throws TwitterClientException {
        List<T> l = new LinkedList<T>();

        long cursor = -1;
        PagableResponseList p;
        do {
            try {
                p = g.getList(cursor);
            } catch (TwitterException e) {
                throw new TwitterClientException(e);
            }
            l.addAll(p);
            cursor = p.getNextCursor();
        } while (p.hasNext());

        return l;
    }

    private interface ListGenerator<T extends TwitterResponse> {
        PagableResponseList getList(long cursor) throws TwitterException;
    }
}
