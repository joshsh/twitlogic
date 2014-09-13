package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.Factory;
import org.openrdf.elmo.ElmoManager;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;

import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TweetStoreConnection {
    private static final Logger LOGGER = TwitLogic.getLogger(TweetStoreConnection.class);

    private final TweetStore tweetStore;
    private final SailConnection sailConnection;
    private ElmoManager elmoManager;

    private boolean closed;

    public TweetStoreConnection(final TweetStore tweetStore,
                                final Factory<SailConnectionListener> listenerFactory) throws TweetStoreException {
        this.tweetStore = tweetStore;

        try {
            this.sailConnection = tweetStore.getSail().getConnection();
            this.sailConnection.begin();

            if (null != listenerFactory && sailConnection instanceof NotifyingSailConnection) {
                SailConnectionListener l = listenerFactory.create();
                ((NotifyingSailConnection) sailConnection).addConnectionListener(l);
            }
        } catch (SailException e) {
            throw new TweetStoreException(e);
        }

        createElmoManager();
    }

    private void createElmoManager() {
        elmoManager = tweetStore.getElmoManagerFactory().createElmoManager();

        // Use an active transaction (rather than using auto-commit mode).
        // We will explicitly call commit() and rollback().
        //    elmoManager.getTransaction().begin();
    }

    public TweetStore getTweetStore() {
        return tweetStore;
    }

    public SailConnection getSailConnection() {
        return sailConnection;
    }

    public ElmoManager getElmoManager() {
        return elmoManager;
    }

    public void commit() throws TweetStoreException {
        try {
            if (!elmoManager.getTransaction().isActive()) {
                LOGGER.warning("Elmo transaction is not active.  Creating a new Elmo manager.");
                createElmoManager();
            }

            elmoManager.getTransaction().commit();
        } finally {
            try {
                sailConnection.commit();
                sailConnection.begin();
            } catch (SailException e) {
                throw new TweetStoreException(e);
            }
        }
    }

    public void rollback() throws TweetStoreException {
        try {
            elmoManager.getTransaction().rollback();
        } finally {
            try {
                sailConnection.rollback();
            } catch (SailException e) {
                throw new TweetStoreException(e);
            }
        }
    }

    public void close() throws TweetStoreException {
        if (!closed) {
            closed = true;

            try {
                elmoManager.close();
            } finally {
                try {
                    sailConnection.rollback();
                    sailConnection.close();
                } catch (SailException e) {
                    throw new TweetStoreException(e);
                }
            }

            tweetStore.notifyClosed(this);
        }
    }
}
