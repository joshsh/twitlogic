package edu.rpi.tw;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.twitter.TwitterClient;

import java.util.Properties;


/**
 * User: josh
 * Date: Apr 1, 2010
 * Time: 6:39:48 PM
 */
public class StockTweets {
    private static final String[] keywords = {
            "#gold", "#oil",
            "$CVX", "$XOM",  // oil, energy
            "$MRK",  // health care
            "$BA",  // companies who consume oil heavily (anything else?)
            "$BAC", "$JPM", "$GS", "$MS",  // finance
            "$WMT",  // consumer retail
            "$BIDU", "$MSFT", "$GOOG"  // technology
    };

    public static void main(final String[] args) {
        try {
            Properties conf = new Properties();
            conf.load(StockTweets.class.getResourceAsStream("stockTweets.properties"));
            TwitLogic.setConfiguration(conf);

            aggregate();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void aggregate() throws Exception {
        // Create a persistent store.
        TweetStore store = new TweetStore();
        store.initialize();

        store.dump(System.out);

        try {
            TwitterClient client = new TwitterClient();

            TweetStoreConnection c = store.createConnection();
            try {
                boolean persistUnannotatedTweets = true;
                TweetPersister baseStatusHandler = new TweetPersister(store, c, client, persistUnannotatedTweets);

                client.processTrackFilterStream(keywords, baseStatusHandler);
            } finally {
                c.close();
            }
        } finally {
            store.shutDown();
        }
    }
}
