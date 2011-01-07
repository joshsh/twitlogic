package net.fortytwo.twitlogic.larkc;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.flow.NullHandler;
import net.fortytwo.twitlogic.logging.TweetReceivedLogger;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreException;
import net.fortytwo.twitlogic.server.TwitLogicServer;
import net.fortytwo.twitlogic.services.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.TopicSniffer;
import net.fortytwo.twitlogic.syntax.TweetAnnotator;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import org.openrdf.model.Statement;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailWrapper;
import org.openrdf.sail.memory.MemoryStore;

import java.util.Set;
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
        StatementQueuingListener l;

        try {
            l = new StatementQueuingListener(this.overflowPolicy);
        } catch (PropertyException e) {
            LOGGER.severe(e.toString());
            throw new IllegalStateException(e);
        }

        SimpleCallback onClose = new SimpleCallback() {
            public void execute() {
                closed = true;
            }
        };

        try {
            start(l);
        } catch (Exception e) {
            LOGGER.severe(e.toString());
            throw new IllegalStateException(e);
        }

        return new StreamingQueueIterator<Statement>(l.getQueue(), onClose);
    }

    public SetOfStatements toRDF(final SetOfStatements setOfStatements) {
        // TODO: is this right?
        return this;
    }

    private void start(final SailConnectionListener listener) throws Exception {
        NotifyingSail baseSail = new MemoryStore();
        baseSail.initialize();

        try {
            NotifyingSail sail = new NotifyingSailWrapper(baseSail);

            // Create a persistent store.
            TweetStore store = new TweetStore(sail);
            store.initialize();

            try {
                // Create a client for communication with Twitter.
                TwitterClient client = new TwitterClient();

                Set<User> users = TwitLogic.findFollowList(client);
                Set<String> terms = TwitLogic.findTrackTerms();

                final Handler<Tweet, TweetHandlerException> annotator
                        = createAnnotator(store, client);

                final NotifyingSailConnection c = sail.getConnection();
                c.addConnectionListener(listener);

                try {
                    Handler<Tweet, TweetHandlerException> adder = new Handler<Tweet, TweetHandlerException>() {
                        public boolean handle(final Tweet tweet) throws TweetHandlerException {
                            try {
                                c.clear();
                                c.commit();
                            } catch (SailException e) {
                                throw new TweetHandlerException(e);
                            }

                            return !closed && annotator.handle(tweet);
                        }
                    };

                    // Can't use a deleter here.
                    NullHandler<Tweet, TweetHandlerException> d = new NullHandler<Tweet, TweetHandlerException>();

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
            baseSail.shutDown();
        }
    }

    private static Handler<Tweet, TweetHandlerException> createAnnotator(final TweetStore store,
                                                                         final TwitterClient client) throws TweetStoreException {
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
