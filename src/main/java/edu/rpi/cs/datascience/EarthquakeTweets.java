package edu.rpi.cs.datascience;

import net.fortytwo.twitlogic.TwitLogicAgent;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.persistence.UserRegistry;
import net.fortytwo.twitlogic.twitter.CommandListener;
import net.fortytwo.twitlogic.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.twitter.TwitterClient;
import net.fortytwo.twitlogic.util.properties.TypedProperties;

/**
 * User: josh
 * Date: Nov 20, 2009
 * Time: 4:17:01 PM
 */
public class EarthquakeTweets {
    public static void main(final String[] args) throws Exception {
        try {
            TypedProperties conf = new TypedProperties();
            conf.load(EarthquakeTweets.class.getResourceAsStream("datascience.properties"));

            // Create a persistent store.
            TweetStore store = new TweetStore(conf);
            store.initialize();

            try {
                store.dump(System.out);

                TwitterClient client = new TwitterClient();
                UserRegistry userRegistry = new UserRegistry(client);
                TweetStoreConnection c = store.createConnection();
                try {
                    Handler<Tweet, TweetHandlerException> baseStatusHandler
                            = new TweetPersister(null, store, c, client, false);

                    // Create an agent to listen for commands.
                    // Also take the opportunity to memoize users we're following.
                    TwitLogicAgent agent = new TwitLogicAgent(client);
                    Handler<Tweet, TweetHandlerException> statusHandler
                            = userRegistry.createUserRegistryFilter(
                            new CommandListener(agent, baseStatusHandler));

                    String[] keywords = {"earthquake"};
                    client.processTrackFilterStream(keywords, statusHandler);

                    System.out.println("Done.");
                } finally {
                    c.close();
                }
            } finally {
                store.shutDown();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
