package net.fortytwo.twitlogic.persistence;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.PlainLiteral;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.TypedLiteral;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MatcherException;
import net.fortytwo.twitlogic.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.twitter.TwitterClientException;
import net.fortytwo.twitlogic.vocabs.SIOC;
import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.vocabs.SIOCT;
import net.fortytwo.twitlogic.vocabs.DCTerms;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Note: not necessarily thread-safe.
 * <p/>
 * User: josh
 * Date: Oct 2, 2009
 * Time: 9:34:51 PM
 */
public class TweetPersister implements Handler<Tweet, TweetHandlerException> {
    private static final Logger LOGGER = TwitLogic.getLogger(TweetPersister.class);

    private final Matcher matcher;
    private final TweetStore store;
    private final Handler<Triple, MatcherException> tripleHandler;
    private final SimpleTweetContext tweetContext;
    private final PersistenceContext persistenceContext;
    private final ValueFactory valueFactory;

    public TweetPersister(final Matcher matcher,
                          final TweetStore store,
                          final PersistenceContext persistenceContext) {
        this.matcher = matcher;
        this.store = store;
        this.persistenceContext = persistenceContext;
        this.valueFactory = store.getSail().getValueFactory();
        this.tweetContext = new SimpleTweetContext();
        this.tripleHandler = new TriplePersister();
    }

    public boolean handle(final Tweet tweet) throws TweetHandlerException {
        System.out.println("" + tweet.getUser().getScreenName()
                + " [" + tweet.getId() + "]"
                + ": " + tweet.getText());

        tweetContext.setCurrentTweet(tweet);
        tweetContext.setCurrentUser(tweet.getUser());

        try {
            matcher.match(tweet.getText(), tripleHandler, tweetContext);
        } catch (MatcherException e) {
            throw new TweetHandlerException(e);
        }

        return true;
    }

    private void describeGraph(final org.openrdf.model.Resource graph,
                               final Tweet source,
                               final SailConnection sc) throws SailException {
        //Date now = new Date();
        sc.addStatement(graph, RDF.TYPE, SesameTools.TRIX_GRAPH, SesameTools.ADMIN_GRAPH);
        //sc.addStatement(graph, SesameTools.TIMESTAMP, SesameTools.createLiteral(now, valueFactory), SesameTools.ADMIN_GRAPH);
        //// TODO: confidence level
        //URI tweetURI = valueOf(source);
        //sc.addStatement(tweetURI, valueFactory.createURI(SIOC.EMBEDSKNOWLEDGE), graph, SesameTools.ADMIN_GRAPH);
    }

    private Statement toRDF(final Triple triple,
                            final org.openrdf.model.Resource graph) throws TwitterClientException {
        Value subject = toRDF(triple.getSubject());
        Value predicate = toRDF(triple.getPredicate());
        Value object = toRDF(triple.getObject());

        if (!(subject instanceof org.openrdf.model.Resource)) {
            LOGGER.warning("triple subject is not a subject resource: " + triple.getSubject());
            return null;
        } else if (!(predicate instanceof URI)) {
            LOGGER.warning("triple predicate is not a predicate resource: " + triple.getPredicate());
            return null;
        } else {
            return valueFactory.createStatement((org.openrdf.model.Resource) subject, (URI) predicate, object, graph);
        }
    }

    private Value toRDF(final Resource resource) throws TwitterClientException {
        switch (resource.getType()) {
            case HASHTAG:
                return valueOf((Hashtag) resource);
            case PLAIN_LITERAL:
                return valueOf((PlainLiteral) resource);
            case TWEET:
                return valueOf((Tweet) resource);
            case TYPED_LITERAL:
                return valueOf((TypedLiteral) resource);
            case URI_REFERENCE:
                return valueOf((URIReference) resource);
            case USER:
                return valueOf((User) resource);
            case PERSON:
                return valueOf((Person) resource);
            default:
                throw new IllegalStateException("unhandled resource type: " + resource.getType());
        }
    }

    private URI valueOf(final Hashtag hashtag) {
        return valueFactory.createURI(persistenceContext.valueOf(hashtag));
    }

    private Literal valueOf(final PlainLiteral literal) {
        return valueFactory.createLiteral(literal.getLabel());
    }

    private URI valueOf(final Tweet tweet) {
        return valueFactory.createURI(persistenceContext.valueOf(tweet));
    }

    private Literal valueOf(final TypedLiteral literal) {
        return valueFactory.createLiteral(literal.getLabel(),
                valueFactory.createURI(literal.getDatatype()));
    }

    private URI valueOf(final URIReference uri) {
        return valueFactory.createURI(uri.getValue());
    }

    private URI valueOf(final User user) throws TwitterClientException {
        return valueFactory.createURI(persistenceContext.valueOf(user));
    }

    private URI valueOf(final Person person) throws TwitterClientException {
        try {
            SailConnection sc = store.getSail().getConnection();
            try {
                URI userURI = valueOf(person.getAccount());
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
                    return heldBy;
                } else {
                    return persistPerson(person, sc);
                }
            } finally {
                sc.close();
            }
        } catch (SailException e) {
            throw new TwitterClientException(e);
        }
    }

    private void ensureUserHasBeenPersisted(final User user) throws TwitterClientException {
        // TODO: this is a hack
        valueOf(user.getHeldBy());
    }

    /*
    private void ensureTweetHasBeenPersisted(final Tweet tweet) throws TwitterClientException {
        URI tweetURI = valueOf(tweet);
        try {
            SailConnection sc = store.getSail().getConnection();
            try {
                boolean exists = false;
                CloseableIteration<? extends Statement, SailException> iter
                        = sc.getStatements(tweetURI, null, null, false, SesameTools.ADMIN_GRAPH);
                try {
                    if (iter.hasNext()) {
                        exists = true;
                    }
                } finally {
                    iter.close();
                }

                if (!exists) {
                    persistTweet(tweet);
                }
            } finally {
                sc.close();
            }
        } catch (SailException e) {
            throw new TwitterClientException(e);
        }
    }*/

    private URI persistTweet(final Tweet tweet) throws TwitterClientException {
        URI graphURI = graphByTweet.get(tweet);
        if (null == graphURI) {
            throw new IllegalStateException("graph should already have been added");
        }

        URI tweetURI = valueOf(tweet);
        User user = tweet.getUser();
        URI userURI = valueOf(user);

        // Note: this causes user and person to be persisted, if they have not already been.
        valueOf(user.getHeldBy());

        try {
            SailConnection sc = store.getSail().getConnection();
            try {
                sc.addStatement(tweetURI,
                        RDF.TYPE,
                        valueFactory.createURI(SIOCT.MICROBLOGPOST),
                        SesameTools.ADMIN_GRAPH);
                sc.addStatement(tweetURI,
                        valueFactory.createURI(SIOC.EMBEDSKNOWLEDGE),
                        graphURI,
                        SesameTools.ADMIN_GRAPH);
                sc.addStatement(tweetURI,
                        valueFactory.createURI(DCTerms.CREATED),
                        // TODO: put these in ISO 8601 format
                        valueFactory.createLiteral(SesameTools.toXMLGregorianCalendar(tweet.getCreatedAt())),
                        SesameTools.ADMIN_GRAPH);
                sc.addStatement(tweetURI,
                        valueFactory.createURI(SIOC.CONTENT),
                        valueFactory.createLiteral(tweet.getText()),
                        SesameTools.ADMIN_GRAPH);
                sc.addStatement(tweetURI,
                        valueFactory.createURI(SIOC.HAS_CREATOR),
                        userURI,
                        SesameTools.ADMIN_GRAPH);

                sc.commit();
            } finally {
                sc.close();
            }
        } catch (SailException e) {
            throw new TwitterClientException(e);
        }

        return tweetURI;
    }

    private final Map<Tweet, URI> graphByTweet = new HashMap<Tweet, URI>();

    private URI persistPerson(final Person person,
                              final SailConnection sc) throws SailException, TwitterClientException {
        User user = persistenceContext.getUserRegistry().findUserInfo(person.getAccount().getScreenName());
        URI userURI = valueOf(user);
        URI personURI = SesameTools.createRandomPersonURI(valueFactory);

        sc.addStatement(personURI,
                RDF.TYPE,
                // not foaf:Person, as not all microblogging accounts belong to people
                valueFactory.createURI(FOAF.AGENT),
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

    private class SimpleTweetContext implements TweetContext {
        private Tweet currentTweet;
        private User currentUser;

        public User thisUser() {
            return currentUser;
        }

        public Person thisPerson() {
            return thisUser().getHeldBy();
        }

        public User repliedToUser() {
            // TODO
            return null;
        }

        public User retweetedUser() {
            // TODO
            return null;
        }

        public Tweet thisTweet() {
            return currentTweet;
        }

        public Tweet repliedToTweet() {
            // TODO
            return null;
        }

        public Resource anonymousNode() {
            // TODO: this is a little stupid.
            return new URIReference(
                    SesameTools.createRandomResourceURI(valueFactory).toString());
        }

        public void setCurrentTweet(final Tweet tweet) {
            this.currentTweet = tweet;
        }

        public void setCurrentUser(final User user) {
            this.currentUser = user;
        }
    }

    private class TriplePersister implements Handler<Triple, MatcherException> {
        public boolean handle(final Triple triple) throws MatcherException {
            // If the tweet from which this triple is taken has not yet been persisted, do so.
            URI graph = graphByTweet.get(tweetContext.thisTweet());
            if (null == graph) {
                graph = SesameTools.createRandomGraphURI(valueFactory);
                graphByTweet.put(tweetContext.thisTweet(), graph);
                try {
                    persistTweet(tweetContext.thisTweet());
                } catch (TwitterClientException e) {
                    throw new MatcherException(e);
                }
            }

            System.out.println("\t (" + triple.getWeight() + ")\t" + triple);
            try {
                SailConnection sc = store.getSail().getConnection();
                try {
                    Statement st = toRDF(triple, graph);
                    if (null != st) {
                        describeGraph(graph, tweetContext.thisTweet(), sc);
                        // TODO: creating a statement and then breaking it into parts is wasty
                        sc.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
                    }
                    sc.commit();
                } catch (TwitterClientException e) {
                    throw new MatcherException(e);
                } finally {
                    sc.close();
                }
            } catch (SailException e) {
                throw new MatcherException(e);
            }

            return true;
        }
    }
}
