package net.fortytwo.twitlogic.util;

import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import net.fortytwo.twitlogic.services.twitter.twitter4j.Twitter4jClient;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ServerTestApp {

    public static void main(final String[] args) throws Exception {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("/Users/josh/projects/fortytwo/twitlogic/config/twitlogic.properties"));
            TwitLogic.setConfiguration(props);

            // Create a persistent store.
            TweetStore store = new TweetStore();
            store.initialize();

            try {
                TwitterClient client = new Twitter4jClient();

                // Launch linked data server.
                store.startServer(client);

                Object mutex = "";
                synchronized (mutex) {
                    mutex.wait();
                }
            } finally {
                store.shutDown();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}