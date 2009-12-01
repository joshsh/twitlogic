package net.fortytwo.twitlogic.persistence;

import org.openrdf.elmo.ElmoManager;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * User: josh
 * Date: Nov 30, 2009
 * Time: 6:00:43 PM
 */
public class TweetStoreConnection {
    private final SailConnection sailConnection;
    private final ElmoManager elmoManager;

    public TweetStoreConnection(final TweetStore tweetStore) throws TweetStoreException {
        try {
            this.sailConnection = tweetStore.getSail().getConnection();
        } catch (SailException e) {
            throw new TweetStoreException(e);
        }

        elmoManager = tweetStore.getElmoManagerFactory().createElmoManager();

        // Use an active transaction (rather than using auto-commit mode).
        // We will explicitly call commit() and rollback().
        elmoManager.getTransaction().begin();
    }

    public SailConnection getSailConnection() {
        return sailConnection;
    }

    public ElmoManager getElmoManager() {
        return elmoManager;
    }

    public void commit() throws TweetStoreException {
        try {
            elmoManager.getTransaction().commit();
        } finally {
            try {
                sailConnection.commit();
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
        try {
            elmoManager.close();
        } finally {
            try {
                sailConnection.close();
            } catch (SailException e) {
                throw new TweetStoreException(e);
            }
        }
    }
}
