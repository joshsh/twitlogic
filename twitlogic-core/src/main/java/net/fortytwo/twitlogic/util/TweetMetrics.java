package net.fortytwo.twitlogic.util;

import net.fortytwo.sesametools.rdftransaction.RDFTransactionSail;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.logging.TweetPersistedLogger;
import net.fortytwo.twitlogic.logging.TweetReceivedLogger;
import net.fortytwo.twitlogic.model.Tweet;
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
import net.fortytwo.twitlogic.util.misc.Compression;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TweetMetrics {
    /*
    java -cp target/twitlogic-0.6-full.jar net.fortytwo.twitlogic.util.TweetMetrics config/metrics.properties > tweet_statistics.txt &
1610 tweet_statistics.txt

scp ubuntu@fortytwo.net:/home/ubuntu/projects/fortytwo/twitlogic/tweet_statistics.txt /tmp
cat /tmp/tweet_statistics.txt | sed 's/^+//' > /tmp/stats.txt
g <- read.table(file("/tmp/stats.txt", encoding="latin1"), comment.char = "")
sum(g[,1])/length(g[,1])
sum(g[,2])/length(g[,2])

6502  -> 5212.153, 804.3202
12131 -> 5213.096, 804.1835
23653 -> 5206.831, 802.8875
     */
    public static void main(final String[] args) {
        try {
            if (1 == args.length) {
                File configFile = new File(args[0]);
                Properties p = new Properties();
                p.load(new FileInputStream(configFile));
                TwitLogic.setConfiguration(p);

                new TweetMetrics().run();
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
        System.out.println("Usage:  TweetMetrics [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://wiki.github.com/joshsh/twitlogic/configuring-and-running-twitlogic>.");
    }

    private void run() throws Exception {
        Sail workingSail = new MemoryStore();
        workingSail.initialize();

        try {
            Sail streamingSail = new MockUdpTransactionSail(workingSail);

            try {
                TweetStore store = new TweetStore(streamingSail);
                store.doNotRefreshCoreMetadata();
                store.initialize();

                try {
                    // A connection with which to repeatedly clear the working store
                    final SailConnection c = workingSail.getConnection();

                    try {
                        // Offline persister
                        final TweetPersister persister = new TweetPersister(store, null);

                        try {
                            CustomTwitterClient client = new CustomTwitterClient();

                            TweetPersistedLogger pLogger = new TweetPersistedLogger(client.getStatistics(), persister);

                            // Add a "topic sniffer".
                            TopicSniffer topicSniffer = new TopicSniffer(pLogger);

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
                                    } catch (SailException e) {
                                        throw new HandlerException(e);
                                    }

                                     annotator.handle(tweet);
                                }
                            };
                            Handler<Tweet> deleter = new TweetDeleter(store);

                            TweetReceivedLogger rLogger = new TweetReceivedLogger(client.getStatistics(), adder);

                            client.processSampleStream(rLogger, deleter);
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

    private class MockUdpTransactionSail extends RDFTransactionSail {
        private final PrintStream ps;

        public MockUdpTransactionSail(final Sail baseSail) {
            super(baseSail);

            try {
                OutputStream os  = new FileOutputStream("/tmp/tweet_metrics.txt");
                ps = new PrintStream(os);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        public void handleTransaction(final List<TransactionOperation> operations) throws SailException {
            byte[] bytes = createTransactionEntity(operations);
            int size = bytes.length;
            int zipSize, gzipSize, lzmaSize, minilzoSize, deflateSize;

            //System.out.println(new String(bytes));

            try {
                gzipSize = Compression.compress(bytes, Compression.Algorithm.GZIP).length;
                lzmaSize = Compression.compress(bytes, Compression.Algorithm.LZMA).length;
                minilzoSize = Compression.compress(bytes, Compression.Algorithm.MINILZO).length;
                zipSize = Compression.compress(bytes, Compression.Algorithm.ZIP).length;
                deflateSize = Compression.compress(bytes, Compression.Algorithm.DEFLATE).length;
            } catch (IOException e) {
                throw new SailException(e);
            }

            ps.println("+\t" + size + "\t" + gzipSize + "\t" + lzmaSize + "\t" + minilzoSize + "\t" + zipSize + "\t" + deflateSize);
        }
    }
}