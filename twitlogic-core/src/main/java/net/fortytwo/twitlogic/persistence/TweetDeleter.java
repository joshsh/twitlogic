package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.persistence.beans.Graph;
import net.fortytwo.twitlogic.persistence.beans.MicroblogPost;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import org.openrdf.elmo.Entity;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import javax.xml.namespace.QName;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Jul 23, 2010
 * Time: 2:44:18 PM
 */
public class TweetDeleter implements Handler<Tweet> {
    private static final Logger LOGGER = TwitLogic.getLogger(TweetDeleter.class);

    private final TweetStoreConnection storeConnection;
    private final PersistenceContext persistenceContext;
    private final ValueFactory valueFactory;

    public TweetDeleter(final TweetStore store) throws TweetStoreException {
        this.storeConnection = store.createConnection();
        this.valueFactory = store.getSail().getValueFactory();
        try {
            this.persistenceContext = new PersistenceContext(storeConnection);
        } catch (PropertyException e) {
            throw new TweetStoreException(e);
        }
    }

    public void close() throws TweetStoreException {
        storeConnection.close();
    }

    public boolean isOpen() {
        return true;
    }

    public void handle(final Tweet tweet) throws HandlerException {
        MicroblogPost p = persistenceContext.find(tweet);

        if (null != p) {
            LOGGER.fine("deleting tweet " + tweet.getId());

            try {
                SailConnection c = storeConnection.getSailConnection();
                try {
                    c.begin();
                    // TODO: remove only from the authoritative graph
                    c.removeStatements(uriOf(p), null, null);

                    Graph g = p.getEmbedsKnowledge();
                    if (null != g) {
                        c.removeStatements(null, null, null, uriOf(g));
                    }

                    c.commit();
                } finally {
                    c.rollback();
                    c.close();
                }
            } catch (SailException e) {
                throw new HandlerException(e);
            }
        }
    }

    private URI uriOf(final Entity e) {
        QName q = e.getQName();
        return valueFactory.createURI(q.getNamespaceURI() + q.getLocalPart());
    }
}
