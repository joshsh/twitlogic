package net.fortytwo.twitlogic.persistence;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.twitter.TwitterClientException;
import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.vocabs.SIOC;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * User: josh
 * Date: Oct 5, 2009
 * Time: 2:07:49 AM
 */
public class PersistenceContext {
    private final UserRegistry userRegistry;
    private final TweetStore store;

    public PersistenceContext(final UserRegistry userRegistry,
                              final TweetStore store) {
        this.userRegistry = userRegistry;
        this.store = store;
    }

    public String valueOf(final Hashtag hashtag) {
        // TODO: assumes normalized hash tags
        return TwitLogic.HASHTAGS_BASEURI + hashtag.getName();
    }

    public String valueOf(final Tweet tweet) {
        return TwitLogic.TWEETS_BASEURI + tweet.getId();
    }

    public String valueOf(final User user) throws TwitterClientException {
        Integer id = user.getId();
        if (null == id) {
            id = userRegistry.resolveUserId(user.getScreenName());
        }

        return TwitLogic.USERS_BASEURI + id;
    }

    public String valueOf(final Person person) throws TwitterClientException {
        ValueFactory valueFactory = store.getSail().getValueFactory();
        try {
            SailConnection sc = store.getSail().getConnection();
            try {
                URI userURI = valueFactory.createURI(valueOf(person.getAccount()));
                URI heldBy = null;
                URI holdsAccount = valueFactory.createURI(FOAF.HOLDSACCOUNT);
                CloseableIteration<? extends Statement, SailException> iter
                        = sc.getStatements(null, holdsAccount, userURI, false, SesameTools.ADMIN_GRAPH);
                try {
                    if (iter.hasNext()) {
                        heldBy = (URI) iter.next().getSubject();
                    }
                } finally {
                    iter.close();
                }

                if (null != heldBy) {
                    return heldBy.toString();
                } else {
                    return persistPerson(person, sc).toString();
                }
            } finally {
                sc.close();
            }
        } catch (SailException e) {
            throw new TwitterClientException(e);
        }
    }


    private URI persistPerson(final Person person,
                              final SailConnection sc) throws SailException, TwitterClientException {
        ValueFactory valueFactory = store.getSail().getValueFactory();
        User user = userRegistry.findUserInfo(person.getAccount().getScreenName());
        URI userURI = valueFactory.createURI(valueOf(user));
        URI personURI = SesameTools.createRandomPersonURI(valueFactory);

        sc.addStatement(personURI,
                RDF.TYPE,
                // not foaf:Person, as not all microblogging accounts belong to people
                valueFactory.createURI(FOAF.AGENT),
                SesameTools.ADMIN_GRAPH);
        // Shout-out to SemanticTweet
        sc.addStatement(personURI,
                OWL.SAMEAS,
                valueFactory.createURI(semanticTweetURI(person.getAccount())),
                SesameTools.ADMIN_GRAPH);
        sc.addStatement(userURI,
                RDF.TYPE,
                valueFactory.createURI(SIOC.USER),
                SesameTools.ADMIN_GRAPH);
        sc.addStatement(personURI,
                valueFactory.createURI(FOAF.HOLDSACCOUNT),
                userURI,
                SesameTools.ADMIN_GRAPH);
        if (null != user.getScreenName()) {
            sc.addStatement(userURI,
                    valueFactory.createURI(SIOC.ID),
                    valueFactory.createLiteral(user.getScreenName()),
                    SesameTools.ADMIN_GRAPH);
        }
        if (null != user.getUrl()) {
            sc.addStatement(personURI,
                    // TODO: type the homepage as foaf:Document
                    valueFactory.createURI(FOAF.HOMEPAGE),
                    valueFactory.createURI(user.getUrl()),
                    SesameTools.ADMIN_GRAPH);
        }
        if (null != user.getName()) {
            sc.addStatement(personURI,
                    valueFactory.createURI(FOAF.NAME),
                    valueFactory.createLiteral(user.getName()),
                    SesameTools.ADMIN_GRAPH);
        }
        // TODO: use user.getLocation() / foaf:based_near
        if (null != user.getProfileImageUrl()) {
            sc.addStatement(personURI,
                    // not foaf:img, because we don't assume the subject is a foaf:Person
                    valueFactory.createURI(FOAF.DEPICTION),
                    valueFactory.createURI(user.getProfileImageUrl()),
                    SesameTools.ADMIN_GRAPH);
        }

        sc.commit();

        return personURI;
    }

    private String semanticTweetURI(final User user) {
        if (null == user.getScreenName()) {
            throw new IllegalArgumentException("null screen name");
        } else {
            return "http://semantictweet.com/" + user.getScreenName() + "#me";
        }
    }

    public UserRegistry getUserRegistry() {
        return userRegistry;
    }
}
