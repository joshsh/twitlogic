package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.flow.Filter;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.services.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Jun 29, 2010
 * Time: 5:08:15 PM
 */
public class PlacesDemo {
    private static final Logger LOGGER = TwitLogic.getLogger(PlacesDemo.class);

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
        System.out.println("Usage:  places-demo [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://wiki.github.com/joshsh/twitlogic/configuring-and-running-twitlogic>.");
    }

    private static void runDemo() throws Exception {
        TweetStore store = new TweetStore();
        store.initialize();

        store.dumpToFile(new File("/tmp/places-demo-dump.nt"), RDFFormat.NTRIPLES);

        try {
            TwitterClient client = new TwitterClient();

            TweetPersister p = new TweetPersister(store, client);

            TweetFilterCriterion crit = new TweetFilterCriterion();
            crit.setAllowsTweetsWithPlace(true);
            Filter<Tweet, TweetHandlerException> f = new Filter<Tweet, TweetHandlerException>(crit, p);

            client.processSampleStream(f);
        } finally {
            store.shutDown();
        }
    }
}
