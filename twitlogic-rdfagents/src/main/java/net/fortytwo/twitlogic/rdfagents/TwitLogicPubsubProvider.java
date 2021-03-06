package net.fortytwo.twitlogic.rdfagents;

import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubProvider;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
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
import net.fortytwo.twitlogic.persistence.TweetStoreException;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.services.twitter.CustomTwitterClient;
import net.fortytwo.twitlogic.services.twitter.TwitterClientException;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MultiMatcher;
import net.fortytwo.twitlogic.syntax.TopicSniffer;
import net.fortytwo.twitlogic.syntax.TweetAnnotator;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.helpers.NotifyingSailWrapper;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: 6/1/11
 * Time: 4:41 PM
 */
public class TwitLogicPubsubProvider extends PubsubProvider<Value, Dataset> {
    private static final Logger LOGGER = TwitLogic.getLogger(TwitLogicPubsubProvider.class);

    private boolean active = false;
    private long minimumUpdateInterval = 0;
    private long lastUpdate = 0;

    private Sail sail;

    public TwitLogicPubsubProvider(final RDFAgent agent,
                                   final Properties config) throws Exception {
        super(agent);

        TwitLogic.setConfiguration(config);

        final Handler<Dataset> handler = new Handler<Dataset>() {
            public boolean isOpen() {
                return true;
            }

            public void handle(Dataset dataset) throws HandlerException {
                try {
                    handleDataset(dataset);
                } catch (LocalFailure e) {
                    throw new HandlerException(e);
                }
            }
        };

        // Do this here so that the Sail is initialized before the runner thread starts.
        final TweetGenerator g = new TweetGenerator();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // TODO: shut down the generator
                    g.run(handler);
                } catch (Throwable e) {
                    active = false;
                    LOGGER.severe("TwitLogic thread failed (stack trace follows)\n" + RDFAgents.stackTraceToString(e));
                }
            }
        }).start();
    }

    public void setMinimumUpdateInterval(final long minimumUpdateInterval) {
        this.minimumUpdateInterval = minimumUpdateInterval;
    }

    public boolean isActive() {
        return active;
    }


    public Sail getSail() {
        return sail;
    }

    @Override
    protected Commitment considerSubscriptionRequestInternal(final Value topic,
                                                             final AgentId initiator) {
        return new Commitment(Commitment.Decision.AGREE_AND_NOTIFY, null);
    }

    public class TweetGenerator {

        private final Collection<Statement> buffer = new LinkedList<Statement>();
        private final TweetStore store;

        public TweetGenerator() throws TweetStoreException, SailException {

            SailConnectionListener listener = new SailConnectionListener() {
                @Override
                public void statementAdded(Statement statement) {
                    buffer.add(statement);
                }

                @Override
                public void statementRemoved(Statement statement) {
                    // Do nothing.
                }
            };

            Sail baseSail = TweetStore.createSail();
            NotifyingSail b;
            if (baseSail instanceof NotifyingSail) {
                b = (NotifyingSail) baseSail;
            } else {
                b = new NotifyingSailWrapper();
                ((NotifyingSailWrapper) b).setBaseSail(baseSail);
            }
            sail = new WrapperNotifyingSail(b, listener);
            sail.initialize();

            store = new TweetStore(sail);
        }

        public void run(final Handler<Dataset> datasetHandler) throws TweetStoreException, TwitterClientException, PropertyException {
            store.initialize();
            try {
                final TweetPersister persister = new TweetPersister(store, null);
                try {
                    Handler<Tweet> handler = new Handler<Tweet>() {
                        @Override
                        public boolean isOpen() {
                            return persister.isOpen() && datasetHandler.isOpen();
                        }

                        @Override
                        public void handle(final Tweet tweet) throws HandlerException {
                            System.out.println("got this tweet: " + tweet);

                            buffer.clear();
                            persister.handle(tweet);

                            if (0 < buffer.size()) {
                                Collection<Statement> c = new LinkedList<Statement>();
                                c.addAll(buffer);
                                datasetHandler.handle(new Dataset(c));
                            }
                        }
                    };

                    CustomTwitterClient client = new CustomTwitterClient();

                    TweetPersistedLogger pLogger = new TweetPersistedLogger(client.getStatistics(), handler);
                    TweetFilterCriterion crit = new TweetFilterCriterion(TwitLogic.getConfiguration());
                    Filter<Tweet> f = new Filter<Tweet>(crit, pLogger);

                    // Add a "topic sniffer".
                    TopicSniffer topicSniffer = new TopicSniffer(f);

                    // Add a tweet annotator.
                    Matcher matcher = new MultiMatcher(
                            new DemoAfterthoughtMatcher());

                    final Handler<Tweet> annotator
                            = new TweetAnnotator(matcher, topicSniffer);

                    Handler<Tweet> deleter = new TweetDeleter(store);

                    TweetReceivedLogger rLogger = new TweetReceivedLogger(client.getStatistics(), annotator);

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
                store.shutDown();
            }
        }
    }

    private class WrapperNotifyingSail extends NotifyingSailBase {
        private final NotifyingSail base;
        private final SailConnectionListener listener;

        public WrapperNotifyingSail(final NotifyingSail base, SailConnectionListener listener) {
            this.base = base;
            this.listener = listener;
        }

        @Override
        protected void shutDownInternal() throws SailException {
            base.shutDown();
        }

        @Override
        protected NotifyingSailConnection getConnectionInternal() throws SailException {
            NotifyingSailConnection c = base.getConnection();
            c.addConnectionListener(listener);
            return c;
        }

        @Override
        public boolean isWritable() throws SailException {
            return base.isWritable();
        }

        @Override
        public ValueFactory getValueFactory() {
            return base.getValueFactory();
        }
    }

    protected void handleDataset(final Dataset d) throws LocalFailure {
        Set<Value> t = getTopics();

        long now = new Date().getTime();

        if (now - lastUpdate >= minimumUpdateInterval) {
            lastUpdate = now;

            for (Statement s : d.getStatements()) {
                if (t.contains(s.getSubject())) {
                    produceUpdate(s.getSubject(), d);
                } else if (t.contains(s.getObject())) {
                    produceUpdate(s.getObject(), d);
                }
            }
        }
    }
}
