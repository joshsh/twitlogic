package net.fortytwo.twitlogic.util;

import net.fortytwo.twitlogic.TweetFilterCriterion;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.server.TwitLogicServer;
import net.fortytwo.twitlogic.flow.Filter;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.logging.TweetPersistedLogger;
import net.fortytwo.twitlogic.logging.TweetReceivedLogger;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.TweetDeleter;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.services.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.TopicSniffer;
import net.fortytwo.twitlogic.syntax.TweetAnnotator;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
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
 * User: josh
 * Date: Aug 23, 2010
 * Time: 12:21:28 PM
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

        //String host = "localhost";
        //String host = "fluxdmz";
        //int port = 9990;

        InetAddress address = InetAddress.getByName(host);

        Sail workingSail = new MemoryStore();
        workingSail.initialize();

        try {
            Sail streamingSail = new UdpTransactionSail(workingSail, address, ports);

            try {
                TweetStore store = new TweetStore(streamingSail);
                store.doNotRefreshCoreMetadata();
                store.initialize();

                // Note: this is only for serving local files.
                new TwitLogicServer(store);

                try {
                    // A connection with which to repeatedly clear the working store
                    final SailConnection c = workingSail.getConnection();

                    try {
                        // Offline persister
                        final TweetPersister persister = new TweetPersister(store, null);

                        try {
                            TwitterClient client = new TwitterClient();

                            TweetPersistedLogger pLogger = new TweetPersistedLogger(client.getStatistics(), persister);
                            TweetFilterCriterion crit = new TweetFilterCriterion(TwitLogic.getConfiguration());
                            Filter<Tweet, TweetHandlerException> f = new Filter<Tweet, TweetHandlerException>(crit, pLogger);

                            // Add a "topic sniffer".
                            TopicSniffer topicSniffer = new TopicSniffer(f);

                            // Add a tweet annotator.
                            Matcher matcher = new MultiMatcher(
                                    new DemoAfterthoughtMatcher());

                            final Handler<Tweet, TweetHandlerException> annotator
                                    = new TweetAnnotator(matcher, topicSniffer);

                            Handler<Tweet, TweetHandlerException> adder = new Handler<Tweet, TweetHandlerException>() {
                                public boolean handle(final Tweet tweet) throws TweetHandlerException {
                                    try {
                                        c.clear();
                                        c.commit();
                                    } catch (SailException e) {
                                        throw new TweetHandlerException(e);
                                    }

                                    return annotator.handle(tweet);
                                }
                            };
                            Handler<Tweet, TweetHandlerException> deleter = new TweetDeleter(store);

                            TweetReceivedLogger rLogger = new TweetReceivedLogger(client.getStatistics(), adder);

                            Set<User> users = TwitLogic.findFollowList(client);
                            Set<String> terms = TwitLogic.findTrackTerms();

                            if (0 < users.size() || 0 < terms.size()) {
                                client.processFilterStream(users, terms, rLogger, deleter, 0);
                            } else {
                                client.processSampleStream(rLogger, deleter);
                            }
                        } finally {
                            persister.close();
                        }
                    } finally {
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

    /*
    private abstract class TweetHandler implements Handler<Tweet, TweetHandlerException> {
        private final Handler<Tweet, TweetHandlerException> handler;

        public TweetHandler(final Handler<Tweet, TweetHandlerException> handler) {
            this.handler = handler;
        }

        public boolean handle(final Tweet tweet) throws TweetHandlerException {
            return handler.handle(tweet);
        }

        public TweetHandler append(final Handler<Tweet, TweetHandlerException> other) {

        }
    }*/
}
