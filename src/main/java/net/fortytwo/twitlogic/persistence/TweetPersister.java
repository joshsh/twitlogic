package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Hashtag;
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
import net.fortytwo.twitlogic.vocabs.SIOC;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.Date;
import java.util.Random;
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
    private final ValueFactory valueFactory;

    public TweetPersister(final Matcher matcher,
                          final TweetStore store) {
        this.matcher = matcher;
        this.store = store;
        this.valueFactory = store.getSail().getValueFactory();
        this.tweetContext = new SimpleTweetContext();
        this.tripleHandler = new Handler<Triple, MatcherException>() {
            public boolean handle(final Triple triple) throws MatcherException {
                System.out.println("\t (" + triple.getWeight() + ")\t" + triple);
                try {
                    SailConnection sc = store.getSail().getConnection();
                    try {
                        org.openrdf.model.Resource graph = SesameTools.createRandomURI(valueFactory);
                        Statement st = toRDF(triple, graph);
                        if (null != st) {
                            describeGraph(graph, tweetContext.thisTweet(), sc);
                            // TODO: creating a statement and then breaking it into parts is wasty
                            sc.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
                        }
                        sc.commit();
                    } finally {
                        sc.close();
                    }
                } catch (SailException e) {
                    throw new MatcherException(e);
                }

                return true;
            }
        };
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
        Date now = new Date();
        sc.addStatement(graph, RDF.TYPE, SesameTools.TRIX_GRAPH, SesameTools.ADMIN_GRAPH);
        sc.addStatement(graph, SesameTools.TIMESTAMP, SesameTools.createLiteral(now, valueFactory), SesameTools.ADMIN_GRAPH);
        // TODO: confidence level
        URI tweetURI = valueOf(source);
        sc.addStatement(tweetURI, valueFactory.createURI(SIOC.EMBEDSKNOWLEDGE), graph, SesameTools.ADMIN_GRAPH);
    }

    private Statement toRDF(final Triple triple,
                            final org.openrdf.model.Resource graph) {
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

    private Value toRDF(final Resource resource) {
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
            default:
                throw new IllegalStateException("unhandled resource type: " + resource.getType());
        }
    }

    private URI valueOf(final Hashtag hashtag) {
        // TODO: assumes normalized hash tags
        return valueFactory.createURI(SesameTools.HASHTAG_PREFIX + hashtag.getName());
    }

    private Literal valueOf(final PlainLiteral literal) {
        return valueFactory.createLiteral(literal.getLabel());
    }

    private URI valueOf(final Tweet tweet) {
        return valueFactory.createURI(SesameTools.TWEET_PREFIX + tweet.getId());
    }

    private Literal valueOf(final TypedLiteral literal) {
        return valueFactory.createLiteral(literal.getLabel(),
                valueFactory.createURI(literal.getDatatype()));
    }

    private URI valueOf(final URIReference uri) {
        return valueFactory.createURI(uri.getValue());
    }

    private URI valueOf(final User user) {
        Integer id = user.getId();
        if (null == id) {
            throw new IllegalArgumentException("tried to rdfize user with null id: " + user);
        }
        return valueFactory.createURI(SesameTools.USERS_PREFIX + id);
    }

    private class SimpleTweetContext implements TweetContext {
        private final Random random = new Random();

        private Tweet currentTweet;
        private User currentUser;

        public User thisUser() {
            return currentUser;
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
            // TODO: put this in a common location
            return new URIReference(
                    SesameTools.RANDOMURI_PREFIX + random.nextInt(Integer.MAX_VALUE));
        }

        public void setCurrentTweet(final Tweet tweet) {
            this.currentTweet = tweet;
        }

        public void setCurrentUser(final User user) {
            this.currentUser = user;
        }
    }
}
