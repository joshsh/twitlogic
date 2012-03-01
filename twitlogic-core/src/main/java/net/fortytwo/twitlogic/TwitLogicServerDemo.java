package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.logging.TweetPersistedLogger;
import net.fortytwo.twitlogic.logging.TweetReceivedLogger;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.TweetDeleter;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.persistence.TweetStoreException;
import net.fortytwo.twitlogic.services.twitter.CustomTwitterClient;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import net.fortytwo.twitlogic.services.twitter.TwitterClientException;
import net.fortytwo.twitlogic.services.twitter.twitter4j.Twitter4jClient;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.TopicSniffer;
import net.fortytwo.twitlogic.syntax.TweetAnnotator;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TwitLogicServerDemo {
    private static final Logger LOGGER = TwitLogic.getLogger(TwitLogicServerDemo.class);

    public static void main(final String[] args) {
        try {
            String file;
            if (0 == args.length) {
                file = "/tmp/twitlogic.props";
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
            t.printStackTrace();
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
            // Launch linked data server.
            store.startServer();

            // Create a client for communication with Twitter.
            TwitterClient client = new Twitter4jClient();
            //TwitterClient client = new CustomTwitterClient();

            Handler<Tweet> annotator
                    = createAnnotator(store, client);

            // Create an agent to listen for commands.
            // Also take the opportunity to memoize users we're following.
            /*
            TwitLogicAgent agent = new TwitLogicAgent(client);
            UserRegistry userRegistry = new UserRegistry(client);
            Handler<Tweet, TweetHandlerException> realtimeStatusHandler
                    = userRegistry.createUserRegistryFilter(
                    new CommandListener(agent, annotator));
            */

            Set<User> users = TwitLogic.findFollowList(client);
            Set<String> terms = TwitLogic.findTrackTerms();

            GregorianCalendar cal = new GregorianCalendar(2010, GregorianCalendar.MAY, 1);
            //gatherHistoricalTweets(store, client, users, cal.getTime());

            TweetReceivedLogger rLogger = new TweetReceivedLogger(client.getStatistics(), annotator);
            TweetDeleter d = new TweetDeleter(store);
            client.processFilterStream(users, terms, rLogger, d, 0);
        } finally {
            store.shutDown();
        }
    }

    private static void gatherHistoricalTweets(final TweetStore store,
                                               final CustomTwitterClient client,
                                               final Set<User> users,
                                               final Date startTime) throws TweetStoreException, TwitterClientException, HandlerException {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    TweetStoreConnection c = store.createConnection();
                    try {
                        // Note: don't run old tweets through the command listener, or
                        // TwitLogic will respond, annoyingly, to old commands.
                        client.processTimelineFrom(users, startTime, new Date(), createAnnotator(store, client));
                    } finally {
                        c.close();
                    }
                } catch (Throwable t) {
                    LOGGER.severe("historical tweets thread died with error: " + t);
                    t.printStackTrace();
                }
            }
        });

        t.start();
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
