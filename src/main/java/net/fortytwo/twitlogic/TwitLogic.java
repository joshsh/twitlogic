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
            new TwitterUser(71631722, "antijosh"),      // TwitLogic test account

            // Some TWC-Twitterers (note: I think there are more...)
            new TwitterUser(14731308, "baojie"),        // Jie Bao
            new TwitterUser(15336340, "jahendler"),     // James Hendler
            new TwitterUser(15477931, "taswegian"),     // Peter Fox
            new TwitterUser(17346783, "ankesh_k"),      // Ankesh Khandelwal
            new TwitterUser(18003181, "difrad"),        // Dominic DiFranzo
            new TwitterUser(19122108, "dlmcguinness"),  // Deborah McGuinness
            new TwitterUser(19274805, "shangz"),        // Zhenning Shangguan
            new TwitterUser(20830884, "jrweave"),       // Jesse Weaver
            new TwitterUser(26823198, "lidingpku"),     // Li Ding
            new TwitterUser(33308444, "xixiluo"),       // Xixi Luo
            new TwitterUser(34309130, "ewpatton"),      // Evan Patton
            new TwitterUser(39816942, "alvarograves"),  // Alvaro Graves
            new TwitterUser(643563, "kasei"),           // Gregory Williams
            new TwitterUser(7083182, "joshsh"),         // Joshua Shinavier

            // Others
            new TwitterUser(71089109, "twarko")};       // Marko Rodriguez

    private static String[] aFewGoodUserIds() {
        String[] ids = new String[aFewGoodUsers.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = "" + aFewGoodUsers[i].getId();
        }
        return ids;
    }

}
