package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.twitter.TwitterSecurity;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Sep 3, 2009
 * Time: 1:55:26 PM
 */
public class TwitLogic {

    // Configuration property keys
    public static final String
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
            NAMESPACE = "http://fortytwo.net/2009/10/twitlogic#",
            RESOURCES_NAMESPACE = "http://twitlogic.fortytwo.net/resources/";

    public static final URI
            ASSOCIATION = new URIImpl(NAMESPACE + "Association"),
            SUBJECT = new URIImpl(NAMESPACE + "subject"),
            OBJECT = new URIImpl(NAMESPACE + "object"),
            WORD = new URIImpl(NAMESPACE + "Word"),
            WEIGHT = new URIImpl(NAMESPACE + "weight");

    private static final Properties configuration;
    private static final Logger LOGGER;

    static {
        configuration = new Properties();

        try {
            configuration.load(TwitLogic.class.getResourceAsStream("twitlogic.properties"));
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

    public static Properties getConfiguration() {
        return configuration;
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c.getName());
    }

    public static void main(final String[] args) throws Exception {
        try {
            TwitterSecurity t = new TwitterSecurity();

//        deriveCredentials();

            t.loadCredentials();

            Handler<Tweet, Exception> statusHandler = new ExampleStatusHandler();

             t.processFollowFilterStream(aFewGoodUserIds(), statusHandler, 0);
            //t.processSampleStream(statusHandler);
            //t.processTrackFilterStream(new String[]{"twitter"}, new ExampleStatusHandler());
            //t.processTrackFilterStream(new String[]{"twit","logic","parkour","semantic","rpi"}, new ExampleStatusHandler());
            //t.processFollowFilterStream(new String[]{"71631722","71089109","12","13","15","16","20","87"}, new ExampleStatusHandler());

//        t.makeRequest();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static final User[] A_FEW_GOOD_USERS = new User[]{
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
