package net.fortytwo.twitlogic.util.misc;

import net.fortytwo.twitlogic.TweetFilterCriterion;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Filter;
import net.fortytwo.twitlogic.logging.TweetPersistedLogger;
import net.fortytwo.twitlogic.logging.TweetReceivedLogger;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.TweetDeleter;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.services.twitter.CustomTwitterClient;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
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

        //store.dumpToFile(new File("/tmp/places-demo-dump.nt"), RDFFormat.NTRIPLES);

        try {
            CustomTwitterClient client = new CustomTwitterClient();

            store.startServer(client);

            TweetPersister persister = new TweetPersister(store, client);
            TweetDeleter d = new TweetDeleter(store);

            TweetPersistedLogger pLogger = new TweetPersistedLogger(client.getStatistics(), persister);
            TweetFilterCriterion crit = new TweetFilterCriterion(TwitLogic.getConfiguration());
            Filter<Tweet> f = new Filter<Tweet>(crit, pLogger);
            TweetReceivedLogger rLogger = new TweetReceivedLogger(client.getStatistics(), f);

            Set<User> users = TwitLogic.findFollowList(client);
            Set<String> terms = TwitLogic.findTrackTerms();

            if (0 < users.size() || 0 < terms.size()) {
                client.processFilterStream(users, terms, null, rLogger, d, 0);
            } else {
                client.processSampleStream(rLogger, d);
            }
        } finally {
            store.shutDown();
        }
    }

}
