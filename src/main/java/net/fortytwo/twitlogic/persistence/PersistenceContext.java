package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.persistence.beans.Agent;
import net.fortytwo.twitlogic.persistence.beans.Document;
import net.fortytwo.twitlogic.persistence.beans.Graph;
import net.fortytwo.twitlogic.persistence.beans.Image;
import net.fortytwo.twitlogic.persistence.beans.MicroblogPost;
import net.fortytwo.twitlogic.persistence.beans.SpatialThing;
import net.fortytwo.twitlogic.persistence.beans.User;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.ElmoManager;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 * Note: the (private) "persist" methods of the class use an "add only" approach:
 * functional relationships such as names and homepages are only added or
 * replaced, never removed.  So if a user removes its homepage link,
 * TwitLogic will remember the old homepage until the user chooses a new,
 * valid homepage URL.
 * <p/>
 * User: josh
 * Date: Nov 23, 2009
 * Time: 10:31:14 PM
 */
public class PersistenceContext {
    private final ElmoManager manager;

    public PersistenceContext(final ElmoManager manager) {
        this.manager = manager;
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
                && TwitLogic.URL_PATTERN.matcher(tweetUser.getUrl()).matches()) {
            // Note: we can't easily delete an existing homepage (removing its
            // rdf:type statement), as it might be the homepage of another
            // agent.  Therefore, "orphaned" Document resources are possible.

            Document homepage = manager.designate(new QName(tweetUser.getUrl()), Document.class);
            agent.setHomepage(homepage);
        }

        if (null != tweetUser.getProfileImageUrl()
                && TwitLogic.URL_PATTERN.matcher(tweetUser.getProfileImageUrl()).matches()) {
            // Note: we can't easily delete an existing image (removing its
            // rdf:type statement), as it might be the image of another
            // agent.  Therefore, "orphaned" Image resources are possible.

            Image depiction = manager.designate(new QName(tweetUser.getProfileImageUrl()), Image.class);
            agent.setDepiction(depiction);
        }

        return user;
    }

    public Thing persist(final Hashtag hashtag) {
        return designate(uriOf(hashtag), Thing.class);
    }

    public Agent persist(final net.fortytwo.twitlogic.model.Person tweetPerson) {
        net.fortytwo.twitlogic.model.User tweetUser = tweetPerson.getAccount();
        User user = persist(tweetUser);
        return user.getAccountOf();
    }

    ////////////////////////////////////////////////////////////////////////////

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

    private MicroblogPost postForTweet(final Tweet tweet) {
        return designate(uriOf(tweet), MicroblogPost.class);
    }

    private Graph graphForTweet(final Tweet tweet) {
        String uri = TwitLogic.GRAPHS_BASEURI + "twitter/" + tweet.getId();
        return designate(uri, Graph.class);
    }

    ////////////////////////////////////////////////////////////////////////////

    private <T> T designate(final String uri,
                            final Class<T> c) {
        return manager.designate(new QName(uri), c);
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

    public static String uriOf(final Hashtag hashtag) {
        // FIXME: assumes normalized hash tags
        return TwitLogic.HASHTAGS_BASEURI + hashtag.getName();
    }
}
