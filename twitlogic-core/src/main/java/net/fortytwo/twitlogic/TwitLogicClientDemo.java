package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.logging.TweetPersistedLogger;
import net.fortytwo.twitlogic.logging.TweetReceivedLogger;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.TweetDeleter;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreException;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import net.fortytwo.twitlogic.services.twitter.TwitterClientException;
import net.fortytwo.twitlogic.services.twitter.twitter4j.Twitter4jClient;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.TopicSniffer;
import net.fortytwo.twitlogic.syntax.TweetAnnotator;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
import net.fortytwo.twitlogic.util.ExampleTweetHandler;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TwitLogicClientDemo {
    private static final Logger LOGGER = TwitLogic.getLogger(TwitLogicClientDemo.class);

    public static void main(final String[] args) {
        try {
            String file;
            if (0 == args.length) {
                file = "/tmp/twitlogic.properties";
            } else if (1 == args.length) {
                file = args[0];
            } else {
                file = null;
                printUsage();
                System.exit(1);
            }

            File configFile = new File(file);
            Properties p = new Properties();
            p.load(new FileInputStream(configFile));
            TwitLogic.setConfiguration(p);

            runDemo();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:  twitlogic [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://wiki.github.com/joshsh/twitlogic/configuring-and-running-twitlogic>.");
    }

    private static void runDemo() throws Exception {
        // Create a persistent store.
        TweetStore store = new TweetStore();
        store.initialize();

        try {
            // Create a client for communication with Twitter.
            //TwitterClient client = new Twitter4jClient();
            TwitterClient client = new Twitter4jClient();

            Handler<Tweet> annotator
                    = createAnnotator(store, client);

            Set<User> users = TwitLogic.findFollowList(client);
            Set<String> terms = TwitLogic.findTrackTerms();
            double [][]locations = TwitLogic.findGeoBoxes();

//            GregorianCalendar cal = new GregorianCalendar(2010, GregorianCalendar.MAY, 1);
//            //gatherHistoricalTweets(store, client, users, cal.getTime());

            TweetReceivedLogger rLogger = new TweetReceivedLogger(client.getStatistics(), annotator);
            TweetDeleter d = new TweetDeleter(store);

            ExampleTweetHandler h = new ExampleTweetHandler();
//            client.processFilterStream(users, terms, rLogger, d, 0);
            client.processFilterStream(users, terms, locations, h, d, 0);
            //client.processSampleStream(rLogger, d);
        } finally {
            store.shutDown();
        }
    }

    private static Handler<Tweet> createAnnotator(final TweetStore store,
                                                  final TwitterClient client) throws TweetStoreException, TwitterClientException {
        // Create the tweet persister.
        TweetPersister persister = new TweetPersister(store, client);
        TweetPersistedLogger pLogger = new TweetPersistedLogger(client.getStatistics(), persister);

        // Add a "topic sniffer".
        TopicSniffer topicSniffer = new TopicSniffer(pLogger);

        // Add a tweet annotator.
        Matcher matcher = new MultiMatcher(
                new DemoAfterthoughtMatcher());

        return new TweetAnnotator(matcher, topicSniffer);
    }
}
