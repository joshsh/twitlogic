package net.fortytwo.twitlogic.util;

import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.TwitLogic;

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
                // Launch linked data server.
                store.startServer();

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