package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.server.TwitLogicServer;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.TopicSniffer;
import net.fortytwo.twitlogic.syntax.TweetAnnotator;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
import net.fortytwo.twitlogic.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.twitter.TwitterClient;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;

/**
 * User: josh
 * Date: Apr 14, 2010
 * Time: 7:45:53 PM
 */
public class TwitLogicDemo {

    public static void main(final String[] args) {
        try {
            if (1 == args.length) {
                File configFile = new File(args[0]);
                Properties p = new Properties();
                p.load(new FileInputStream(configFile));
                TwitLogic.setConfiguration(p);

                aggregate();
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

    private static void aggregate() throws Exception {
        // Create a persistent store.
        TweetStore store = new TweetStore();
        store.initialize();

        try {
            // Launch linked data server.
            new TwitLogicServer(store);

            // Create a client for communication with Twitter.
            TwitterClient client = new TwitterClient();

            TweetStoreConnection c = store.createConnection();
            try {
                // Create the tweet persister.
                boolean persistUnannotatedTweets = true;
                TweetPersister persister = new TweetPersister(store, c, client, persistUnannotatedTweets);

                // Add a "topic sniffer".
                TopicSniffer topicSniffer = new TopicSniffer(persister);

                // Add a tweet annotator.
                Matcher matcher = new MultiMatcher(//new TwipleMatcher(),
                        new DemoAfterthoughtMatcher());
                Handler<Tweet, TweetHandlerException> annotator
                        = new TweetAnnotator(matcher, topicSniffer);

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

                /*
                // Gather historical tweets
                {
                    GregorianCalendar cal = new GregorianCalendar(2009, 10, 1);


                    // Note: don't run old tweets through the command listener, or
                    // TwitLogic will respond, annoyingly, to old commands.
                    client.processTimelineFrom(users, cal.getTime(), annotator);
                }
                //*/

                client.processFollowFilterStream(users, annotator, 0);
            } finally {
                c.close();
            }
        } finally {
            store.shutDown();
        }
    }
}
