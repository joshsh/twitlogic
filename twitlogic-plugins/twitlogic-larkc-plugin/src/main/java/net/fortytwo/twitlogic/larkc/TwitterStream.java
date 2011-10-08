package net.fortytwo.twitlogic.larkc;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.flow.NullHandler;
import net.fortytwo.twitlogic.larkc.sail.QueueingSail;
import net.fortytwo.twitlogic.logging.TweetReceivedLogger;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreException;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.services.twitter.CustomTwitterClient;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.TopicSniffer;
import net.fortytwo.twitlogic.syntax.TweetAnnotator;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
import net.fortytwo.twitlogic.util.Factory;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import org.openrdf.model.Statement;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

/**
 * A streaming SetOfStatements implementation which draws statements from a stream of RDFized tweets.
 * <p/>
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TwitterStream extends StreamingSetOfStatements {
    private static final Logger LOGGER = TwitLogic.getLogger(TwitterStream.class);

    private boolean closed = false;

    public TwitterStream(final StreamingPlugin.OverflowPolicy overflowPolicy) {
        super(overflowPolicy);
    }

    public CloseableIterator<Statement> getStatements() {
        final StatementQueuingListener l;

        int capacity;
        try {
            capacity = TwitLogic.getConfiguration().getInt(TwitLogicPlugin.QUEUE_CAPACITY, TwitLogicPlugin.DEFAULT_QUEUE_CAPACITY);
        } catch (PropertyException e) {
            LOGGER.severe(e.toString());
            throw new IllegalStateException(e);
        }

        final ArrayBlockingQueue<Statement> queue = new ArrayBlockingQueue<Statement>(capacity);

        final Factory<SailConnectionListener> factory = new Factory<SailConnectionListener>() {
            public SailConnectionListener create() {
                try {
                    return new StatementQueuingListener(queue, overflowPolicy);
                } catch (PropertyException e) {
                    LOGGER.severe(e.toString());
                    throw new IllegalStateException(e);
                }
            }
        };

        SimpleCallback onClose = new SimpleCallback() {
            public void execute() {
                closed = true;
            }
        };

        new Thread(new Runnable() {
            public void run() {
                try {
                    start(factory);
                } catch (Exception e) {
                    LOGGER.severe(e.toString());
                    throw new IllegalStateException(e);
                }
            }
        }).start();

        return new StreamingQueueIterator<Statement>(queue, onClose);
    }

    public SetOfStatements toRDF(final SetOfStatements setOfStatements) {
        // TODO: is this right?
        return this;
    }

    private void start(final Factory<SailConnectionListener> factory) throws Exception {
        NotifyingSail baseSail = new MemoryStore();
        baseSail.initialize();
        Sail sail = new QueueingSail(baseSail, factory.create());

        try {
            // Create a persistent store.
            TweetStore store = new TweetStore(sail);
            store.setSailConnectionListenerFactory(factory);
            store.doNotRefreshCoreMetadata();
            store.initialize();

            try {
                // Create a client for communication with Twitter.
                CustomTwitterClient client = new CustomTwitterClient();

                Set<User> users = TwitLogic.findFollowList(client);
                Set<String> terms = TwitLogic.findTrackTerms();

                final Handler<Tweet> annotator
                        = createAnnotator(store, client);

                final SailConnection c = sail.getConnection();
                //c.addConnectionListener(listener);

                try {
                    Handler<Tweet> adder = new Handler<Tweet>() {
                        public boolean handle(final Tweet tweet) throws HandlerException {
                            try {
                                c.clear();
                                c.commit();
                            } catch (SailException e) {
                                throw new HandlerException(e);
                            }

                            return !closed && annotator.handle(tweet);
                        }
                    };

                    // Can't use a deleter here.
                    NullHandler<Tweet> d = new NullHandler<Tweet>();

                    // TODO: optionally gather historical tweets

                    TweetReceivedLogger rLogger = new TweetReceivedLogger(client.getStatistics(), adder);

                    if (0 < users.size() || 0 < terms.size()) {
                        client.processFilterStream(users, terms, rLogger, d, 0);
                    } else {
                        client.processSampleStream(rLogger, d);
                    }
                } finally {
                    c.close();
                }
            } finally {
                store.shutDown();
            }
        } finally {
            sail.shutDown();
        }
    }

    private static Handler<Tweet> createAnnotator(final TweetStore store,
                                                  final CustomTwitterClient client) throws TweetStoreException {
        // Create the tweet persister.
        TweetPersister persister = new TweetPersister(store, client);

        // Add a "topic sniffer".
        TopicSniffer topicSniffer = new TopicSniffer(persister);

        // Add a tweet annotator.
        Matcher matcher = new MultiMatcher(
                new DemoAfterthoughtMatcher());

        return new TweetAnnotator(matcher, topicSniffer);
    }
}
