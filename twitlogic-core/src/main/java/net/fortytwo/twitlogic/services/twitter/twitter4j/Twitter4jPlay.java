package net.fortytwo.twitlogic.services.twitter.twitter4j;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.services.twitter.TwitterCredentials;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Twitter4jPlay {
    public static void main(final String[] args) throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream(new File("/tmp/twitlogic.props")));
        TwitLogic.setConfiguration(props);

        TwitterCredentials cred = new TwitterCredentials();

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(cred.getConsumerKey())
                .setOAuthConsumerSecret(cred.getConsumerSecret())
                .setOAuthAccessToken(cred.getAccessToken())
                .setOAuthAccessTokenSecret(cred.getTokenSecret());

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        List<Status> statuses = twitter.getHomeTimeline();

        System.out.println("Showing friends timeline.");
        for (Status status : statuses) {
            System.out.println(status.getUser().getName() + ":" +
                    status.getText());
        }


    }
}
