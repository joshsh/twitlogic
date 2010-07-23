package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.TweetDeleter;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.persistence.TweetStoreException;
import net.fortytwo.twitlogic.server.TwitLogicServer;
import net.fortytwo.twitlogic.services.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import net.fortytwo.twitlogic.services.twitter.TwitterClientException;
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
 * User: josh
 * Date: Apr 14, 2010
 * Time: 7:45:53 PM
 */
public class TwitLogicDemo {
    private static final Logger LOGGER = TwitLogic.getLogger(TwitLogicDemo.class);

    public static void main(final String[] args) {
        try {
            if (1 == args.length) {
                File configFile = new File(args[0]);
                Properties p = new Properties();
                p.load(new FileInputStream(configFile));
                TwitLogic.setConfiguration(p);

                runDemo();
            } else {
                printUsage();
                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
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
            new TwitLogicServer(store);

            // Create a client for communication with Twitter.
            TwitterClient client = new TwitterClient();

            Handler<Tweet, TweetHandlerException> annotator
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

            TweetDeleter d = new TweetDeleter(store);
            client.processFollowFilterStream(users, terms, annotator, d, 0);
        } finally {
            store.shutDown();
        }
    }

    private static void gatherHistoricalTweets(final TweetStore store,
                                               final TwitterClient client,
                                               final Set<User> users,
                                               final Date startTime) throws TweetStoreException, TwitterClientException, TweetHandlerException {
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

    private static Handler<Tweet, TweetHandlerException> createAnnotator(final TweetStore store,
                                                                         final TwitterClient client) throws TweetStoreException {
        // Create the tweet persister.
        TweetPersister persister = new TweetPersister(store, client);

        // Add a "topic sniffer".
        TopicSniffer topicSniffer = new TopicSniffer(persister);

        // Add a tweet annotator.
        Matcher matcher = new MultiMatcher(
                new DemoAfterthoughtMatcher());

        return new TweetAnnotator(matcher, topicSniffer);
    }
}
