package net.fortytwo.twitlogic;

import java.util.Properties;
import java.io.IOException;

/**
 * Author: josh
 * Date: Sep 3, 2009
 * Time: 1:55:26 PM
 */
public class TwitLogic {
    // Configuration property keys
    public static final String
            TWITTER_CONSUMER_KEY = "net.fortytwo.twitlogic.twitterConsumerKey",
            TWITTER_CONSUMER_SECRET = "net.fortytwo.twitlogic.twitterConsumerSecret",
            TWITTER_ACCESS_TOKEN = "net.fortytwo.twitlogic.twitterAccessToken",
            TWITTER_ACCESS_TOKEN_SECRET = "net.fortytwo.twitlogic.twitterAccessTokenSecret";

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

    public static Properties getConfiguration() {
        return configuration;
    }
}
