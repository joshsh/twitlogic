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

    // TODO: make this configurable
    private static final long UPDATE_THRESHOLD = 24 * 60 * 60 * 1000;

    // TODO: make this configurable
    private static final int FOLLOWEES_LIMIT = 1000;

    public PersonResource() throws Exception {
    }

    @Override
    public void preprocessingHook() throws Exception {
        URI personURI = sail.getValueFactory().createURI(this.selfURI);

        TweetStore store = TweetStore.getInstance();
        TweetStoreConnection c = store.createConnection();
        try {
            ValueFactory vf = store.getSail().getValueFactory();
            SailConnection sc = c.getSailConnection();

            // check timestamp
            long lastUpdate = 0;
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
            if (now - lastUpdate > UPDATE_THRESHOLD) {
                LOGGER.info((0 == lastUpdate ? "setting" : "updating") + " followees of " + personURI);

                // fetch followees
                TwitterClient client = store.getTwitterClient();
                long id = Long.valueOf(selfURI.substring(selfURI.lastIndexOf('/') + 1));
                User user = new User(id);
                List<User> followees = client.getFollowees(user, FOLLOWEES_LIMIT);

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
                        DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
                sc.removeStatements(personURI, TwitlogicVocabulary.lastUpdatedAt, null);
                sc.addStatement(personURI, TwitlogicVocabulary.lastUpdatedAt, obj);

                sc.commit();
            }
        } finally {
            c.close();
        }
    }
}
