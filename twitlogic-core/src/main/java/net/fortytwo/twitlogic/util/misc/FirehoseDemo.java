package net.fortytwo.twitlogic.util.misc;

import net.fortytwo.twitlogic.TweetFilterCriterion;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Filter;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.logging.TweetPersistedLogger;
import net.fortytwo.twitlogic.logging.TweetReceivedLogger;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.TweetDeleter;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.services.twitter.CustomTwitterClient;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.TopicSniffer;
import net.fortytwo.twitlogic.syntax.TweetAnnotator;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
import net.fortytwo.twitlogic.util.UdpTransactionSail;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class FirehoseDemo {

    public static void main(final String[] args) {
        try {
            if (1 == args.length) {
                File configFile = new File(args[0]);
                Properties p = new Properties();
                p.load(new FileInputStream(configFile));
                TwitLogic.setConfiguration(p);

                new FirehoseDemo().run();
            } else {
                printUsage();
                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:  firehose [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://wiki.github.com/joshsh/twitlogic/configuring-and-running-twitlogic>.");
    }

    private int[] parsePorts(final String portsStr) {
        String[] a = portsStr.split(",");
        int[] ports = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            ports[i] = Integer.valueOf(a[i].trim());
        }
        return ports;
    }

    private void run() throws Exception {
        TypedProperties config = TwitLogic.getConfiguration();
        String host = config.getString(TwitLogic.UDP_REMOTEHOST);
        String portsStr = config.getString(TwitLogic.UDP_REMOTEPORTS);
        int[] ports = parsePorts(portsStr);

        InetAddress address = InetAddress.getByName(host);

        Sail workingSail = new MemoryStore();
        workingSail.initialize();

        try {
            Sail streamingSail = new UdpTransactionSail(workingSail, address, ports);

            try {
                TweetStore store = new TweetStore(streamingSail);
                store.doNotRefreshCoreMetadata();
                store.initialize();

                try {
                    // A connection with which to repeatedly clear the working store
                    final SailConnection c = workingSail.getConnection();

                    try {
                        c.begin();

                        // Offline persister
                        final TweetPersister persister = new TweetPersister(store, null);

                        try {
                            CustomTwitterClient client = new CustomTwitterClient();

                            // Note: this is only for serving local files.
                            store.startServer(client);

                            TweetPersistedLogger pLogger = new TweetPersistedLogger(client.getStatistics(), persister);
                            TweetFilterCriterion crit = new TweetFilterCriterion(TwitLogic.getConfiguration());
                            Filter<Tweet> f = new Filter<Tweet>(crit, pLogger);

                            // Add a "topic sniffer".
                            TopicSniffer topicSniffer = new TopicSniffer(f);

                            // Add a tweet annotator.
                            Matcher matcher = new MultiMatcher(
                                    new DemoAfterthoughtMatcher());

                            final Handler<Tweet> annotator
                                    = new TweetAnnotator(matcher, topicSniffer);

                            Handler<Tweet> adder = new Handler<Tweet>() {
                                public boolean isOpen() {
                                    return annotator.isOpen();
                                }

                                public void handle(final Tweet tweet) throws HandlerException {
                                    try {
                                        c.clear();
                                        c.commit();
                                        c.begin();
                                    } catch (SailException e) {
                                        throw new HandlerException(e);
                                    }

                                     annotator.handle(tweet);
                                }
                            };
                            Handler<Tweet> deleter = new TweetDeleter(store);

                            TweetReceivedLogger rLogger = new TweetReceivedLogger(client.getStatistics(), adder);

                            Set<User> users = TwitLogic.findFollowList(client);
                            Set<String> terms = TwitLogic.findTrackTerms();

                            if (0 < users.size() || 0 < terms.size()) {
                                client.processFilterStream(users, terms, null, rLogger, deleter, 0);
                            } else {
                                client.processSampleStream(rLogger, deleter);
                            }
                        } finally {
                            persister.close();
                        }
                    } finally {
                        c.rollback();
                        c.close();
                    }
                } finally {
                    store.shutDown();
                }
            } finally {
                streamingSail.shutDown();
            }
        } finally {
            workingSail.shutDown();
        }
    }
}
