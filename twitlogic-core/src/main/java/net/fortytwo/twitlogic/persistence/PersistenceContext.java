package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.Dollartag;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Place;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.model.geo.Point;
import net.fortytwo.twitlogic.persistence.beans.Agent;
import net.fortytwo.twitlogic.persistence.beans.Document;
import net.fortytwo.twitlogic.persistence.beans.Feature;
import net.fortytwo.twitlogic.persistence.beans.Graph;
import net.fortytwo.twitlogic.persistence.beans.Image;
import net.fortytwo.twitlogic.persistence.beans.MicroblogPost;
import net.fortytwo.twitlogic.persistence.beans.SpatialThing;
import net.fortytwo.twitlogic.persistence.beans.User;
import net.fortytwo.twitlogic.syntax.TweetSyntax;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.ElmoManager;
import org.openrdf.elmo.Entity;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Note: the (private) "persist" methods of the class use an "add only" approach:
 * functional relationships such as names and homepages are only added or
 * replaced, never removed.  So if a user removes its homepage link,
 * TwitLogic will remember the old homepage until the user chooses a new,
 * valid homepage URL.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class PersistenceContext {
    private static final Logger LOGGER = TwitLogic.getLogger(PersistenceContext.class);

    private final ElmoManager manager;
    private final boolean avoidRedundantTypeDesignation;

    public PersistenceContext(final ElmoManager manager) throws PropertyException {
        this.manager = manager;
        avoidRedundantTypeDesignation = TwitLogic.getConfiguration().getBoolean(
                TwitLogic.AVOID_REDUNDANT_TYPE_DESIGNATION, false);
    }

    public MicroblogPost persist(final Tweet tweet,
                                 final boolean persistGraph) {
        MicroblogPost post = postForTweet(tweet);

        if (persistGraph) {
            post.setEmbedsKnowledge(graphForTweet(tweet));
        }

        if (null != tweet.getCreatedAt()) {
            // TODO: put these in the ISO 8601 format
            post.setCreated(SesameTools.toXMLGregorianCalendar(tweet.getCreatedAt()));
        }

        if (null != tweet.getText()) {
            post.setContent(tweet.getText());
        }

        if (null != tweet.getUser()) {
            User user = userForUser(tweet.getUser());
            post.setHasCreator(user);
        }

        if (null != tweet.getInReplyToTweet()) {
            MicroblogPost p = postForTweet(tweet.getInReplyToTweet());
            post.setReplyOf(p);
        }

        // Note: we assume that no tweet will be simultaneously a retweet of one tweet and a reply to another.
        if (null != tweet.getRetweetOf()) {
            MicroblogPost p = postForTweet(tweet.getRetweetOf());
            post.setReplyOf(p);
        }

        Set<Thing> topics = new HashSet<Thing>();
        if (null != tweet.getEntities()) {
            for (Resource t : tweet.getEntities().getTopics()) {
                if (t instanceof Hashtag) {
                    topics.add(persist((Hashtag) t));
                } else if (t instanceof Dollartag) {
                    topics.add(persist((Dollartag) t));
                } else {
                    throw new IllegalStateException("topic of unfamiliar type: " + t);
                }
            }
        }
        post.setTopic(topics);

        Set<Thing> links = new HashSet<Thing>();
        if (null != tweet.getEntities()) {
            for (URIReference t : tweet.getEntities().getLinks()) {
                try {
                    links.add(persist(t));
                } catch (IllegalArgumentException e) {
                    LOGGER.warning("invalid URI: " + t);
                    // Just skip this link.
                }
            }
        }
        post.setLinksTo(links);

        /*
        if (null != tweet.getInReplyToUser()) {
            User user = userForUser(tweet.getInReplyToUser());
            post.setAddressedTo(user);
        }*/

        // TODO: geo

        return post;
    }

    public User persist(final net.fortytwo.twitlogic.model.User tweetUser) {
        User user = userForUser(tweetUser);

        if (null != tweetUser.getScreenName()) {
            user.setId(tweetUser.getScreenName());
        }

        Agent agent = user.getAccountOf();

        if (null == agent) {
            agent = agentForUser(tweetUser);
            user.setAccountOf(agent);
        }

        Set<Thing> equivalentAgents = new HashSet<Thing>();
        String semanticTweetUri
                = "http://semantictweet.com/" + tweetUser.getScreenName() + "#me";
        equivalentAgents.add(
                designate(semanticTweetUri, Thing.class));
        agent.setOwlSameAs(equivalentAgents);

        if (null != tweetUser.getName()) {
            agent.setName(tweetUser.getName());
        }

        if (null != tweetUser.getDescription()) {
            agent.setRdfsComment(tweetUser.getDescription());
        }

        if (null != tweetUser.getLocation()) {
            SpatialThing basedNear = agent.getBasedNear();
            if (null == basedNear) {
                basedNear = spatialThing();
                agent.setBasedNear(basedNear);
            }

            basedNear.setRdfsComment(tweetUser.getLocation());
        }

        if (null != tweetUser.getUrl()
                && TweetSyntax.URL_PATTERN.matcher(tweetUser.getUrl()).matches()) {
            // Note: we can't easily delete an existing homepage (removing its
            // rdf:type statement), as it might be the homepage of another
            // agent.  Therefore, "orphaned" Document resources are possible.

            Document homepage = designate(tweetUser.getUrl(), Document.class);
            agent.setHomepage(homepage);
        }

        if (null != tweetUser.getProfileImageUrl()
                && TweetSyntax.URL_PATTERN.matcher(tweetUser.getProfileImageUrl()).matches()) {
            // Note: we can't easily delete an existing image (removing its
            // rdf:type statement), as it might be the image of another
            // agent.  Therefore, "orphaned" Image resources are possible.

            Image depiction = designate(tweetUser.getProfileImageUrl(), Image.class);
            agent.setDepiction(depiction);
        }

        return user;
    }

    public Thing persist(final Dollartag tag) {
        return designate(uriOf(tag), Thing.class);
    }

    public Thing persist(final Hashtag hashtag) {
        return designate(uriOf(hashtag), Thing.class);
    }

    public Thing persist(final URIReference uri) {
        return designate(uri.getValue(), Thing.class);
    }

    public Agent persist(final net.fortytwo.twitlogic.model.Person tweetPerson) {
        net.fortytwo.twitlogic.model.User tweetUser = tweetPerson.getAccount();
        User user = persist(tweetUser);
        return user.getAccountOf();
    }

    public net.fortytwo.twitlogic.persistence.beans.Point persist(final Point point) {
        // TODO: create a URI based on longitude and latitude. It won't be unique due to varying precision, but close enough.
        net.fortytwo.twitlogic.persistence.beans.Point p = point();
        p.setLong(point.getLongitude());
        p.setLat(point.getLatitude());
        return p;
    }

    public Feature persist(final Place place) {
        Feature f = (Feature) designate(uriOf(place), place.getPlaceType().getElmoClass());

        if (null != place.getName()) {
            f.setRdfsLabel(place.getName());
        }

        if (null != place.getFullName()) {
            f.setTitle(place.getFullName());
        }

        if (null != place.getUrl()) {
            Thing t = designate(place.getUrl(), Thing.class);
            // FIXME: I'm not sure how this Object Set is handled
            Set<Object> s = f.getRdfsSeeAlso();
            s.add(t);
            f.setRdfsSeeAlso(s);
        }

        // TODO: link into DBPedia and/or GeoNames
        if (null != place.getCountryCode()) {
            f.setCountryCode(place.getCountryCode());
        }

        /*
        if (null != place.getPlaceType()) {
            org.openrdf.concepts.rdfs.Class c = classForPlaceType(place.getPlaceType());

            if (null != c) {
                Set<org.openrdf.concepts.rdfs.Class> types = f.getRdfTypes();
                types.add(c);
                f.setRdfTypes(types);
            }
        }*/

        // Note: double-typing the feature as a Point is cheating (but it works...)
        if (null != place.getCentroid()) {
            Point c = place.getCentroid();
            net.fortytwo.twitlogic.persistence.beans.Point p
                    = designate(uriOf(place), net.fortytwo.twitlogic.persistence.beans.Point.class);

            p.setLong(c.getLongitude());
            p.setLat(c.getLatitude());
        }

        return f;
    }

    ////////////////////////////////////////////////////////////////////////////

    public MicroblogPost find(final Tweet tweet) {
        Entity e = manager.find(new QName(uriOf(tweet)));
        return null == e
                ? null

                : e instanceof MicroblogPost
                ? (MicroblogPost) e
                : null;
    }

    ////////////////////////////////////////////////////////////////////////////

    //private org.openrdf.concepts.rdfs.Class classForPlaceType(final PlaceType type) {
    //    return designate(type.getUri(), org.openrdf.concepts.rdfs.Class.class);
    //}

    private User userForUser(final net.fortytwo.twitlogic.model.User user) {
        return designate(uriOf(user), User.class);
    }

    private Agent agentForUser(final net.fortytwo.twitlogic.model.User user) {
        return designate(uriOf(user.getHeldBy()), Agent.class);
    }

    private SpatialThing spatialThing() {
        String uri = TwitLogic.LOCATIONS_BASEURI + SesameTools.randomIdString();
        return designate(uri, SpatialThing.class);
    }

    private net.fortytwo.twitlogic.persistence.beans.Point point() {
        String uri = TwitLogic.LOCATIONS_BASEURI + SesameTools.randomIdString();
        return designate(uri, net.fortytwo.twitlogic.persistence.beans.Point.class);
    }

    private MicroblogPost postForTweet(final Tweet tweet) {
        try {
            return designate(uriOf(tweet), MicroblogPost.class);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("bad value: " + uriOf(tweet), e);
        }
    }

    private Graph graphForTweet(final Tweet tweet) {
        String uri = TwitLogic.GRAPHS_BASEURI + "twitter/" + tweet.getId();
        return designate(uri, Graph.class);
    }

    ////////////////////////////////////////////////////////////////////////////

    private <T> T designate(final String uri,
                            final Class<T> c) {
        // Avoid duplicate rdf:type statements (e.g. in AllegroGraph) by first
        // removing matching rdf:type statements before adding a new one.
        // Note: it is still possible for an entity to have multiple, distinct
        // types (e.g. geo:Feature and geo:Point).
        if (avoidRedundantTypeDesignation) {
            T t = manager.designate(new QName(uri), c);
            manager.removeDesignation(t, c);
            return manager.designate(new QName(uri), c);
        } else {
            return manager.designate(new QName(uri), c);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public static String uriOf(final net.fortytwo.twitlogic.model.User user) {
        return TwitLogic.USERS_BASEURI + user.getId();
    }

    public static String uriOf(final Person person) {
        return TwitLogic.PERSONS_BASEURI + "twitter/" + person.getAccount().getId();
    }

    public static String uriOf(final Tweet tweet) {
        return TwitLogic.TWEETS_BASEURI + tweet.getId();
    }

    public static String uriOf(final Dollartag tag) {
        // FIXME: assumes normalized dollar tags
        return TwitLogic.DOLLARTAGS_BASEURI + tag.getName();
    }

    public static String uriOf(final Hashtag hashtag) {
        // FIXME: assumes normalized hash tags
        return TwitLogic.HASHTAGS_BASEURI + hashtag.getName();
    }

    public static String uriOf(final Place place) {
        return TwitLogic.TWITTER_PLACE_BASEURI + place.getId();
    }
}
