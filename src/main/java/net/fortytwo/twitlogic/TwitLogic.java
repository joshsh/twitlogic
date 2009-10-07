package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.PersistenceContext;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.UserRegistry;
import net.fortytwo.twitlogic.server.TwitLogicServer;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
import net.fortytwo.twitlogic.twitter.CommandListener;
import net.fortytwo.twitlogic.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.twitter.TwitterClient;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.io.IOException;
import java.io.InputStream;
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
            COVERAGE_INTERVAL_START = "net.fortytwo.twitlogic.coverageIntervalStart",
            COVERAGE_INTERVAL_END = "net.fortytwo.twitlogic.coverageIntervalEnd",
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

    public static final String
            BASE_URI = "http://twitlogic.fortytwo.net/",
            RESOURCES_BASEURI = BASE_URI + "resource/",
            GRAPHS_BASEURI = RESOURCES_BASEURI + "graph/",
            HASHTAGS_BASEURI = RESOURCES_BASEURI + "hashtag/",
            PERSONS_BASEURI = RESOURCES_BASEURI + "person/",
            TWEETS_BASEURI = RESOURCES_BASEURI + "post/twitter/",
            USERS_BASEURI = RESOURCES_BASEURI + "user/twitter/";

    public static final Pattern
            HASHTAG_PATTERN = Pattern.compile("#[A-Za-z0-9-_]+"),
            USERNAME_PATTERN = Pattern.compile("@[A-Za-z0-9-_]+"),
            URL_PATTERN = Pattern.compile("http://[A-Za-z0-9-]+([.][A-Za-z0-9-]+)*(/([A-Za-z0-9-_#&+./=?~]*[A-Za-z0-9-/])?)?");

    private static final TypedProperties CONFIGURATION;
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

    public static String getName() {
        return "TwitLogic";
    }

    public static String getVersion() {
        return "0.1";
    }

    public static TypedProperties getConfiguration() {
        return CONFIGURATION;
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c.getName());
    }

    public static void main(final String[] args) throws Exception {
        try {
            // Create a persistent store.
            TweetStore store = TweetStore.getDefaultStore();
            store.dump(System.out);

            // Launch linked data server.
            new TwitLogicServer(store);

            TwitterClient client = new TwitterClient();
            UserRegistry userRegistry = new UserRegistry(client);
            PersistenceContext pContext = new PersistenceContext(userRegistry, store);

            // Create the tweet matcher.
            Matcher matcher = new MultiMatcher(//new TwipleMatcher(),
                    new DemoAfterthoughtMatcher());
            Handler<Tweet, TweetHandlerException> baseStatusHandler = new TweetPersister(matcher, store, pContext);

            // Create an agent to listen for commands.
            // Also take the opportunity to memoize users we're following.
            String agentScreenName = TwitLogic.getConfiguration().getString(TwitLogic.TWITTER_USERNAME);
            TwitLogicAgent agent = new TwitLogicAgent(agentScreenName, client, pContext);
            Handler<Tweet, TweetHandlerException> statusHandler
                    = userRegistry.createUserRegistryFilter(
                    new CommandListener(agent, baseStatusHandler));

            //client.requestUserTimeline(new User(71631722), statusHandler);
            client.processFollowFilterStream(aFewGoodUserIds(), statusHandler, 0);
            //client.processSampleStream(statusHandler);
            //client.processTrackFilterStream(new String[]{"twitter"}, new ExampleStatusHandler());
            //client.processTrackFilterStream(new String[]{"twit","logic","parkour","semantic","rpi"}, new ExampleStatusHandler());
            //client.processFollowFilterStream(new String[]{"71631722","71089109","12","13","15","16","20","87"}, new ExampleStatusHandler());
            //*/
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static final User[] A_FEW_GOOD_USERS = new User[]{
            new User("twit_logic", 76195293),    // TwitLogic account
            new User("twitlogic", 62329516),     // aspirational TwitLogic account

            new User("antijosh", 71631722),      // test account
            new User("alvitest", 73477629),      // test account

            // Some TWC-Twitterers (note: I think there are more...)
            new User("alvarograves", 39816942),  // Alvaro Graves
            new User("ankesh_k", 17346783),      // Ankesh Khandelwal
            new User("baojie", 14731308),        // Jie Bao
            new User("difrad", 18003181),        // Dominic DiFranzo
            new User("dlmcguinness", 19122108),  // Deborah McGuinness
            new User("ewpatton", 34309130),      // Evan Patton
            new User("jahendler", 15336340),     // James Hendler
            new User("joshsh", 7083182),         // Joshua Shinavier
            new User("jrweave", 20830884),       // Jesse Weaver
            new User("kasei", 643563),           // Gregory Williams
            new User("lidingpku", 26823198),     // Li Ding
            new User("shangz", 19274805),        // Zhenning Shangguan
            new User("taswegian", 15477931),     // Peter Fox
            new User("xixiluo", 33308444),       // Xixi Luo

            // Twitter Data contributors
            new User("toddfast", 9869202),       // Todd Fast
            new User("jirikopsa", 782594),       // Jiri Kopsa

            // Other friends of the Cause
            new User("twarko", 71089109),        // Marko Rodriguez
            new User("semantictweet", 49974254), // Semantic Tweet
            
            // Miscellaneous people who use a lot of hashtags (not necessarily
            // with TwitLogic in mind).  Adds some healthy "noise" to test the
            // app against inevitable false positives.
            new User("tommyh", 5439642),
            new User("thecoventgarden", 33206959)};

    private static String[] aFewGoodUserIds() {
        String[] ids = new String[A_FEW_GOOD_USERS.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = "" + A_FEW_GOOD_USERS[i].getId();
        }
        return ids;
    }
}
