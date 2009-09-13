package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.twitter.TwitterSecurity;
import net.fortytwo.twitlogic.model.TwitterUser;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * Author: josh
 * Date: Sep 3, 2009
 * Time: 1:55:26 PM
 */
public class TwitLogic {
    private static final Pattern
            WHITESPACE = Pattern.compile("\\s+"),
            NORMAL_TERM = Pattern.compile("[a-z]+([ ][a-z]+)*");

    // Configuration property keys
    public static final String
            TWITTER_CONSUMER_KEY = "net.fortytwo.twitlogic.twitterConsumerKey",
            TWITTER_CONSUMER_SECRET = "net.fortytwo.twitlogic.twitterConsumerSecret",
            TWITTER_ACCESS_TOKEN = "net.fortytwo.twitlogic.twitterAccessToken",
            TWITTER_ACCESS_TOKEN_SECRET = "net.fortytwo.twitlogic.twitterAccessTokenSecret";

    public static final String
            NAMESPACE = "http://fortytwo.net/2009/10/twitlogic#",
            RESOURCES_NAMESPACE = "http://twitlogic.fortytwo.net/resources/";

    public static final URI
            ASSOCIATION = new URIImpl(NAMESPACE + "Association"),
            SOURCE = new URIImpl(NAMESPACE + "source"),
            TARGET = new URIImpl(NAMESPACE + "target"),
            TERM = new URIImpl(NAMESPACE + "Term"),
            WEIGHT = new URIImpl(NAMESPACE + "weight");

    private static final Properties configuration;

    static {
        configuration = new Properties();

        try {
            configuration.load(TwitLogic.class.getResourceAsStream("twitlogic.properties"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
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

    // TODO: move me
    public static String normalizeTerm(final String term) {
        String s = term.trim().toLowerCase();

        Matcher m = WHITESPACE.matcher(s);
        s = m.replaceAll(" ");

        return s;
    }

    public static boolean isNormalTerm(final String term) {
        return NORMAL_TERM.matcher(term).matches();
    }

    public static void main(final String[] args) throws Exception {
        TwitterSecurity t = new TwitterSecurity();

//        deriveCredentials();

        t.loadCredentials();

        //processSampleStream(new ExampleStatusHandler());
        //processTrackFilterStream(new String[]{"twitter"}, new ExampleStatusHandler());
        //processTrackFilterStream(new String[]{"twit","logic","parkour","semantic","rpi"}, new ExampleStatusHandler());
        t.processFollowFilterStream(aFewGoodUserIds(), new ExampleStatusHandler(), 0);
        //processFollowFilterStream(new String[]{"71631722","71089109","12","13","15","16","20","87"}, new ExampleStatusHandler());

//        t.makeRequest();
    }

    private static final TwitterUser[] aFewGoodUsers = new TwitterUser[]{
            new TwitterUser("antijosh", 71631722),      // test account
            new TwitterUser("alvitest", 73477629),      // test account

            // Some TWC-Twitterers (note: I think there are more...)
            new TwitterUser("alvarograves", 39816942),  // Alvaro Graves
            new TwitterUser("ankesh_k", 17346783),      // Ankesh Khandelwal
            new TwitterUser("baojie", 14731308),        // Jie Bao
            new TwitterUser("difrad", 18003181),        // Dominic DiFranzo
            new TwitterUser("dlmcguinness", 19122108),  // Deborah McGuinness
            new TwitterUser("ewpatton", 34309130),      // Evan Patton
            new TwitterUser("jahendler", 15336340),     // James Hendler
            new TwitterUser("joshsh", 7083182),         // Joshua Shinavier
            new TwitterUser("jrweave", 20830884),       // Jesse Weaver
            new TwitterUser("kasei", 643563),           // Gregory Williams
            new TwitterUser("lidingpku", 26823198),     // Li Ding
            new TwitterUser("shangz", 19274805),        // Zhenning Shangguan
            new TwitterUser("taswegian", 15477931),     // Peter Fox
            new TwitterUser("xixiluo", 33308444),       // Xixi Luo

            // Others
            new TwitterUser("twarko", 71089109)};       // Marko Rodriguez

    private static String[] aFewGoodUserIds() {
        String[] ids = new String[aFewGoodUsers.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = "" + aFewGoodUsers[i].getId();
        }
        return ids;
    }

}
