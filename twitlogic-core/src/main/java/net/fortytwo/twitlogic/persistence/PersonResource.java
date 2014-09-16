package net.fortytwo.twitlogic.persistence;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.ldserver.WebResource;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.vocabs.TwitlogicVocabulary;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class PersonResource extends WebResource {
    private static final Logger LOGGER = TwitLogic.getLogger(PersonResource.class);

    private static final long DEFAULT_EXPIRE_TIME = 24 * 60 * 60 * 1000;

    private static final int DEFAULT_FOLLOWEE_LIMIT = 1000;

    private static final DatatypeFactory DATATYPE_FACTORY;

    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final long expireTime;
    private final int followeeLimit;

    public PersonResource() throws Exception {
        expireTime = TwitLogic.getConfiguration().getLong(TwitLogic.METADATA_EXPIRE_TIME, DEFAULT_EXPIRE_TIME);
        followeeLimit = TwitLogic.getConfiguration().getInt(TwitLogic.METADATA_FOLLOWEE_LIMIT, DEFAULT_FOLLOWEE_LIMIT);
    }

    @Override
    public void preprocessingHook() throws Exception {
        TweetStore store = TweetStore.getInstance();
        TweetStoreConnection c = store.createConnection();
        try {
            ValueFactory vf = store.getSail().getValueFactory();
            SailConnection sc = c.getSailConnection();

            long id = Long.valueOf(selfURI.substring(selfURI.lastIndexOf('/') + 1, selfURI.lastIndexOf('.')));
            User user = new User(id);
            URI personURI = vf.createURI(PersistenceContext.uriOf(new Person(user)));

            // check timestamp
            long lastUpdate = -1;
            CloseableIteration<? extends Statement, SailException> iter
                    = sc.getStatements(personURI, TwitlogicVocabulary.lastUpdatedAt, null, false);
            try {
                if (iter.hasNext()) {
                    lastUpdate = ((Literal) iter.next().getObject()).calendarValue().toGregorianCalendar().getTime().getTime();
                }
            } finally {
                iter.close();
            }

            long now = System.currentTimeMillis();
            if (-1 == lastUpdate || (expireTime > 0 && now - lastUpdate > expireTime)) {
                LOGGER.info((0 == lastUpdate ? "setting" : "updating") + " followees of " + personURI);

                // fetch followees
                TwitterClient client = store.getTwitterClient();
                List<User> followees = client.getFollowees(user, followeeLimit);

                URI foafKnows = vf.createURI(FOAF.KNOWS);

                // persist self and foaf:knows edges
                sc.removeStatements(personURI, foafKnows, null);
                for (User f : followees) {
                    sc.addStatement(personURI, foafKnows, vf.createURI(PersistenceContext.uriOf(new Person(f))));
                }

                // update timestamp
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(new Date(now));
                Literal obj = sail.getValueFactory().createLiteral(
                        DATATYPE_FACTORY.newXMLGregorianCalendar(cal));
                sc.removeStatements(personURI, TwitlogicVocabulary.lastUpdatedAt, null);
                sc.addStatement(personURI, TwitlogicVocabulary.lastUpdatedAt, obj);

                sc.commit();
            }
        } finally {
            c.close();
        }
    }
}
