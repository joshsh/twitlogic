package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.CommonHttpClient;
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
import net.fortytwo.twitlogic.persistence.beans.MicroblogPost;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MatcherException;
import net.fortytwo.twitlogic.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.twitter.TwitterClientException;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailException;

import javax.xml.namespace.QName;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Nov 30, 2009
 * Time: 5:39:48 PM
 */
public class TweetPersister implements Handler<Tweet, TweetHandlerException> {
    private static final Logger LOGGER = TwitLogic.getLogger(TweetPersister.class);

    private final Matcher matcher;
    private final TweetStoreConnection storeConnection;
    private final Handler<Triple, MatcherException> tripleHandler;
    private final TweetContext tweetContext;
    private final ValueFactory valueFactory;
    private final PersistenceContext persistenceContext;
    private final CommonHttpClient httpClient;
    private final boolean persistOnlyMatchingTweets;

    private Tweet currentTweet;
    private User currentUser;
    private MicroblogPost currentMicroblogPost;
    private boolean freshTweet;

    public TweetPersister(final Matcher matcher,
                             final TweetStore store,
                             final TweetStoreConnection storeConnection,
                             final CommonHttpClient httpClient,
                             final boolean persistOnlyMatchingTweets) throws TweetStoreException {
        this.matcher = matcher;
        this.storeConnection = storeConnection;
        this.valueFactory = store.getSail().getValueFactory();
        this.tweetContext = new SimpleTweetContext();
        this.tripleHandler = new TriplePersister();
        this.httpClient = httpClient;
        this.persistenceContext = new PersistenceContext(storeConnection.getElmoManager());
        this.persistOnlyMatchingTweets = persistOnlyMatchingTweets;
    }

    public boolean handle(final Tweet tweet) throws TweetHandlerException {
        LOGGER.info("tweet " + tweet.getId()
                + " by @" + tweet.getUser().getScreenName()
                + ": " + tweet.getText());

        try {
            currentTweet = tweet;
            currentUser = tweet.getUser();

            if (persistOnlyMatchingTweets) {
                freshTweet = true;
            } else {
                try {
                    persistTweet(tweet);
                } catch (TwitterClientException e) {
                    throw new TweetHandlerException(e);
                }

                freshTweet = false;
            }

            if (null != matcher) {
                try {
                    matcher.match(tweet.getText(), tripleHandler, tweetContext);
                } catch (MatcherException e) {
                    throw new TweetHandlerException(e);
                }
            }
        } finally {
            try {
                storeConnection.commit();
            } catch (TweetStoreException e) {
                throw new TweetHandlerException(e);
            }
        }

        return true;
    }

    private void persistTweet(final Tweet tweet) throws TwitterClientException {
        currentMicroblogPost = persistenceContext.persist(tweet);
        persistenceContext.persist(tweet.getUser());
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
//            case PERSON:
//                return valueOf((Person) resource);
            default:
                throw new IllegalStateException("unhandled resource type: " + resource.getType());
        }
    }

    private URI valueOf(final Hashtag hashtag) {
        return uriOf(persistenceContext.persist(hashtag));
    }

    private Literal valueOf(final PlainLiteral literal) {
        return valueFactory.createLiteral(literal.getLabel());
    }

    private URI valueOf(final Tweet tweet) {
        return uriOf(persistenceContext.persist(tweet));
    }

    private Literal valueOf(final TypedLiteral literal) {
        return valueFactory.createLiteral(literal.getLabel(),
                valueFactory.createURI(literal.getDatatype()));
    }

    private URI valueOf(final URIReference uri) throws TwitterClientException {
        String nonRedirecting = httpClient.resolve301Redirection(uri.getValue());
        return valueFactory.createURI(nonRedirecting);
    }

    private URI valueOf(final User user) throws TwitterClientException {
        return uriOf(persistenceContext.persist(user));
    }

    private URI uriOf(final Thing thing) {
        QName q = thing.getQName();
        return valueFactory.createURI(q.getNamespaceURI() + q.getLocalPart());
    }

    private class SimpleTweetContext implements TweetContext {
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
    }

    private class TriplePersister implements Handler<Triple, MatcherException> {
        public boolean handle(final Triple triple) throws MatcherException {
            // If the tweet from which this triple is taken has not yet been persisted, do so.
            if (freshTweet) {
                try {
                    persistTweet(tweetContext.thisTweet());
                } catch (TwitterClientException e) {
                    throw new MatcherException(e);
                }

                freshTweet = false;
            }

            System.out.println("\t (" + triple.getWeight() + ")\t" + triple);
            try {
                Statement st = toRDF(triple, uriOf(currentMicroblogPost.getEmbedsKnowledge()));
                if (null != st) {
                    // TODO: creating a statement and then breaking it into parts is wasty
                    try {
                        storeConnection.getSailConnection()
                                .addStatement(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
                    } catch (SailException e) {
                        throw new MatcherException(e);
                    }
                }
            } catch (TwitterClientException e) {
                throw new MatcherException(e);
            }

            return true;
        }
    }
}
