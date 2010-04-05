package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.PeriodicDumpfileGenerator;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.persistence.TweetStoreException;
import net.fortytwo.twitlogic.persistence.UserRegistry;
import net.fortytwo.twitlogic.server.TwitLogicServer;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.TweetAnnotator;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
import net.fortytwo.twitlogic.twitter.CommandListener;
import net.fortytwo.twitlogic.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.twitter.TwitterClient;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Author: josh
 * Date: Sep 3, 2009
 * Time: 1:55:26 PM
 */
public class TwitLogic {

    // Configuration property keys
    public static final String
            BITLY_LOGIN = "net.fortytwo.twitlogic.services.bitly.login",
            BITLY_APIKEY = "net.fortytwo.twitlogic.services.bitly.apiKey",
            NATIVESTORE_DIRECTORY = "net.fortytwo.twitlogic.persistence.nativeStoreDirectory",
            SAIL_CLASS = "net.fortytwo.twitlogic.persistence.sailClass",
            DUMPINTERVAL = "net.fortytwo.twitlogic.persistence.dumpInterval",
            COVERAGE_INTERVAL_START = "net.fortytwo.twitlogic.coverageIntervalStart",
            COVERAGE_INTERVAL_END = "net.fortytwo.twitlogic.coverageIntervalEnd",
            FOLLOWLIST = "net.fortytwo.twitlogic.followList",
            FOLLOWUSER = "net.fortytwo.twitlogic.followUser",
            SERVER_BASEURI = "net.fortytwo.twitlogic.server.baseURI",
            SERVER_PORT = "net.fortytwo.twitlogic.server.port",
            SERVER_STATICCONTENTDIRECTORY = "net.fortytwo.twitlogic.server.staticContentDirectory",
            TWITTER_CONSUMER_KEY = "net.fortytwo.twitlogic.twitter.consumerKey",
            TWITTER_CONSUMER_SECRET = "net.fortytwo.twitlogic.twitter.consumerSecret",
            TWITTER_ACCESS_TOKEN = "net.fortytwo.twitlogic.twitter.accessToken",
            TWITTER_ACCESS_TOKEN_SECRET = "net.fortytwo.twitlogic.twitter.accessTokenSecret",
            TWITTER_USERNAME = "net.fortytwo.twitlogic.twitter.username",
            TWITTER_PASSWORD = "net.fortytwo.twitlogic.twitter.password",
            XMPP_SERVER = "net.fortytwo.twitlogic.xmpp.server",
            XMPP_PORT = "net.fortytwo.twitlogic.xmpp.port",
            XMPP_REPORTER_USERNAME = "net.fortytwo.twitlogic.xmpp.reporterUsername",
            XMPP_REPORTER_PASSWORD = "net.fortytwo.twitlogic.xmpp.reporterPassword",
            XMPP_REASONER_USERNAME = "net.fortytwo.twitlogic.xmpp.reasonerUsername",
            XMPP_REASONER_PASSWORD = "net.fortytwo.twitlogic.xmpp.reasonerPassword";

    public static final String
            NAMESPACE = "http://fortytwo.net/2009/10/twitlogic#";

    public static final URI
            ASSOCIATION = new URIImpl(NAMESPACE + "Association"),
            SUBJECT = new URIImpl(NAMESPACE + "subject"),
            OBJECT = new URIImpl(NAMESPACE + "object"),
            WORD = new URIImpl(NAMESPACE + "Word"),
            WEIGHT = new URIImpl(NAMESPACE + "weight");

    // Note: keep these in agreement with the ResourceType enum.
    public static final String
            BASE_URI = "http://twitlogic.fortytwo.net/",
            DATASETS_BASEURI = BASE_URI + "dataset/",
            DUMPS_BASEURI = BASE_URI + "dump/",
            GRAPHS_BASEURI = BASE_URI + "graph/",
            HASHTAGS_BASEURI = BASE_URI + "hashtag/",
            LOCATIONS_BASEURI = BASE_URI + "location/",
            MISCELLANEOUS_BASEURI = BASE_URI + "miscellaneous/",
            PERSONS_BASEURI = BASE_URI + "person/",
            TWEETS_BASEURI = BASE_URI + "post/twitter/",
            USERS_BASEURI = BASE_URI + "user/twitter/";

    // This graph contains all of TwitLogic's authoritative metadata about
    // named graphs, microblog posts, microblog authors, etc., as well as
    // voiD metadata about the knowledge base itself.
    public static final URI
            AUTHORITATIVE_GRAPH = null;

    // Other special resources
    public static final String
            TWITLOGIC_DATASET = DATASETS_BASEURI + "twitlogic-full",
            SEMANTICTWEET_DATASET = DATASETS_BASEURI + "semantictweet",
            SEMANTICTWEET_LINKSET1 = DATASETS_BASEURI + "semantictweet-linkset1";

    public static final Pattern
            CONFIG_LIST_PATTERN = Pattern.compile("[A-Za-z0-9-_]+/[A-Za-z0-9-_]+"),
            CONFIG_USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9-_]+"),
            HASHTAG_PATTERN = Pattern.compile("#[A-Za-z0-9-_]+"),
            USERNAME_PATTERN = Pattern.compile("@[A-Za-z0-9-_]+"),
            URL_PATTERN = Pattern.compile("http://[A-Za-z0-9-]+([.][A-Za-z0-9-]+)*(/([A-Za-z0-9-_#&+./=?~]*[A-Za-z0-9-/])?)?");

    private static TypedProperties CONFIGURATION;
    private static final Logger LOGGER;

    static {
        CONFIGURATION = new TypedProperties();

        try {
            CONFIGURATION.load(TwitLogic.class.getResourceAsStream("twitlogic.properties"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        InputStream resourceAsStream = TwitLogic.class.getResourceAsStream("logging.properties");

        try {
            LogManager.getLogManager().readConfiguration(resourceAsStream);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER = getLogger(TwitLogic.class);
        LOGGER.info("initialized logging");
    }

    public enum ResourceType {
        DataSet("dataset", "data set"),
        Graph("graph", "named graph"),
        Hashtag("hashtag", "hashtag resource"),
        Location("location", "location"),
        Miscellaneous("miscellaneous", "miscellaneous resource"),
        Person("person", "person"),
        Post("post", "microblog post"),
        User("user", "microblog user account");

        private final String uriPath;
        private final String name;

        private ResourceType(final String uriPath,
                             final String name) {
            this.uriPath = uriPath;
            this.name = name;
        }

        public String getUriPath() {
            return uriPath;
        }

        public String getName() {
            return name;
        }

        public static ResourceType findByName(final String name) {
            for (ResourceType t : ResourceType.values()) {
                if (t.name().equals(name)) {
                    return t;
                }
            }

            return null;
        }
    }

    public static String getName() {
        return "TwitLogic";
    }

    public static String getVersion() {
        return "0.1";
    }

    public static TypedProperties getConfiguration() {
        return CONFIGURATION;
    }

    public static void setConfiguration(final Properties properties) {
        CONFIGURATION = new TypedProperties(properties);
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c.getName());
    }

    public static void main(final String[] args) throws Exception {
        try {
            // Create a persistent store.
            TweetStore store = new TweetStore();
            store.initialize();

            // TODO: this is a hack
            try {
                new Thread(new PeriodicDumpfileGenerator(store, TwitLogic.getConfiguration())).start();
            } catch (PropertyException e) {
                throw new TweetStoreException(e);
            }

            try {
                //store.dump(System.out);

                store.dumpToFile(new File("/tmp/twitlogic-tmp-dump.trig"), RDFFormat.TRIG);
//System.exit(0);
//store.clear();
//store.load(new File("/tmp/twitlogic-tmp-dump.trig"), RDFFormat.TRIG);
//System.exit(0);

                // Launch linked data server.
                new TwitLogicServer(store);

                TwitterClient client = new TwitterClient();
                UserRegistry userRegistry = new UserRegistry(client);
                //PersistenceContext pContext = new PersistenceContext(userRegistry, store);

                TweetStoreConnection c = store.createConnection();
                try {
                    // Create the tweet persister.
                    boolean persistUnannotatedTweets = false;
                    TweetPersister persister = new TweetPersister(store, c, client, persistUnannotatedTweets);

                    // Create the tweet matcher and annotator.
                    Matcher matcher = new MultiMatcher(//new TwipleMatcher(),
                            new DemoAfterthoughtMatcher());
                    Handler<Tweet, TweetHandlerException> annotator
                            = new TweetAnnotator(matcher, persister);

                    // Create an agent to listen for commands.
                    // Also take the opportunity to memoize users we're following.
                    TwitLogicAgent agent = new TwitLogicAgent(client);
                    Handler<Tweet, TweetHandlerException> realtimeStatusHandler
                            = userRegistry.createUserRegistryFilter(
                            new CommandListener(agent, annotator));

                    Set<User> users = findFollowList(client);

                    /*
                    {
                        GregorianCalendar cal = new GregorianCalendar(2009, 10, 1);
                        //GregorianCalendar cal = new GregorianCalendar(2010, 3, 1);


                        // Note: don't run old tweets through the command listener, or
                        // TwitLogic will respond, annoyingly, to old commands.
                        client.processTimelineFrom(users, cal.getTime(), annotator);
                    }
                    //*/

                    client.processFollowFilterStream(users, realtimeStatusHandler, 0);

                    //client.requestUserTimeline(new User(71631722), statusHandler);
                    //client.processFollowFilterStream(A_FEW_GOOD_USERS, statusHandler, 0);
                    //client.processSampleStream(statusHandler);
                    //client.processTrackFilterStream(new String[]{"twitter"}, new ExampleStatusHandler());
                    //client.processTrackFilterStream(new String[]{"twit","logic","parkour","semantic","rpi"}, new ExampleStatusHandler());
                    //client.processTrackFilterStream(new String[]{"#gold", "#oil", "$BAC", "$JPM", "$BA", "$MSFT", "$GOOG", "$GS", "$MS", "$XOM", "$WMT"}, new ExampleStatusHandler());
                    //client.processTrackFilterStream(new String[]{"RT"}, new ExampleStatusHandler());
                    //client.processFollowFilterStream(new String[]{"71631722","71089109","12","13","15","16","20","87"}, new ExampleStatusHandler());
                    //*/
                } finally {
                    c.close();
                }
            } finally {
                store.shutDown();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // Note: for now, lists are not persisted in any way
    public static Set<User> findFollowList(final TwitterClient client) throws Exception {
        TypedProperties props = TwitLogic.getConfiguration();

        // Note: this doesn't really need to be an order-preserving collection,
        // because Java properties are not order-preserving.
        Set<User> users = new LinkedHashSet<User>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(TwitLogic.FOLLOWLIST)) {
                String listVal = props.getString(key);
                if (!CONFIG_LIST_PATTERN.matcher(listVal).matches()) {
                    throw new PropertyException("invalid list: " + listVal + " (should be of the form user_name/list_id)");
                }

                String[] parts = listVal.split("/");
                User user = new User(parts[0]);
                String listId = parts[1];

                List<User> l = client.getListMembers(user, listId);
                users.addAll(l);
            } else if (key.startsWith(TwitLogic.FOLLOWUSER)) {
                String screenName = props.getString(key);
                if (!CONFIG_USERNAME_PATTERN.matcher(screenName).matches()) {
                    throw new PropertyException("invalid screen name: " + screenName);
                }

                // Twitter requires user IDs (as opposed to screen names) for follow filters.
                User u = client.findUserInfo(screenName);
                users.add(u);
            }
        }

        if (0 == users.size()) {
            throw new Exception("no users to follow!  Set the " + TwitLogic.FOLLOWLIST + " property of your configuration to a non-empty list");
        }
        return users;
    }

    /*
private class TweetPrinter implements Handler<Tweet, TweetHandlerException> {
public boolean handle(final Tweet tweet) throws TweetHandlerException {
return false; //To change body of implemented methods use File | Settings | File Templates.
}
}
 
private void showSampleTweets() {
TwitterClient client = new TwitterClient();
client.processSampleStream();
}
*/

/*
    private static final User[] A_FEW_GOOD_USERS = new User[]{
            new User("twit_logic", 76195293), // TwitLogic account
            new User("twitlogic", 62329516), // aspirational TwitLogic account
            new User("datagovwiki", 84994400), // DataGovWiki bot

            new User("antijosh", 71631722), // test account
            new User("alvitest", 73477629), // test account

            // Some TWC-Twitterers (note: I think there are more...)
            new User("alvarograves", 39816942), // Alvaro Graves
            new User("ankesh_k", 17346783), // Ankesh Khandelwal
            new User("baojie", 14731308), // Jie Bao
            new User("difrad", 18003181), // Dominic DiFranzo
            new User("dlmcguinness", 19122108), // Deborah McGuinness
            new User("ewpatton", 34309130), // Evan Patton
            new User("jahendler", 15336340), // James Hendler
            new User("joshsh", 7083182), // Joshua Shinavier
            new User("jpmccu", 47621026), // Jim McCusker
            new User("jrmichaelis", 58557080), // James Michaelis
            new User("jrweave", 20830884), // Jesse Weaver
            new User("kasei", 643563), // Gregory Williams
            new User("lidingpku", 26823198), // Li Ding
            new User("shangz", 19274805), // Zhenning Shangguan
            new User("shankzz", 18604757), // Shankar Arunachalam
            new User("taswegian", 15477931), // Peter Fox
            new User("xixiluo", 33308444), // Xixi Luo

            // Twitter Data contributors
            new User("toddfast", 9869202), // Todd Fast
            new User("jirikopsa", 782594), // Jiri Kopsa

            // Other friends of the Cause
            new User("twarko", 71089109), // Marko Rodriguez
            new User("semantictweet", 49974254), // Semantic Tweet

            // Miscellaneous people who use a lot of hashtags (not necessarily
            // with TwitLogic in mind). Adds some healthy "noise" to test the
            // app against inevitable false positives.
            new User("tommyh", 5439642),
            //new User("thecoventgarden", 33206959)
    };
    */
}

