package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.TwitterClient;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Author: josh
 * Date: Sep 3, 2009
 * Time: 1:55:26 PM
 */
public class TwitLogic {

    // Configuration property keys
    public static final String
            BITLY_LOGIN = "net.fortytwo.twitlogic.services.bitly.login",
            BITLY_APIKEY = "net.fortytwo.twitlogic.services.bitly.apiKey",
            NATIVESTORE_DIRECTORY = "net.fortytwo.twitlogic.persistence.nativeStoreDirectory",
            ALLEGROSAIL_HOST = "net.fortytwo.twitlogic.persistence.allegroSailHost",
            ALLEGROSAIL_PORT = "net.fortytwo.twitlogic.persistence.allegroSailPort",
            ALLEGROSAIL_START = "net.fortytwo.twitlogic.persistence.allegroSailStart",
            ALLEGROSAIL_NAME = "net.fortytwo.twitlogic.persistence.allegroSailName",
            ALLEGROSAIL_DIRECTORY = "net.fortytwo.twitlogic.persistence.allegroSailDirectory",
            SAIL_CLASS = "net.fortytwo.twitlogic.persistence.sailClass",
            DUMP_FILE = "net.fortytwo.twitlogic.persistence.dump.file",
            DUMP_INTERVAL = "net.fortytwo.twitlogic.persistence.dump.interval",
            COVERAGE_INTERVAL_START = "net.fortytwo.twitlogic.coverageIntervalStart",
            COVERAGE_INTERVAL_END = "net.fortytwo.twitlogic.coverageIntervalEnd",
            FOLLOWLIST = "net.fortytwo.twitlogic.followList",
            FOLLOWUSER = "net.fortytwo.twitlogic.followUser",
            TRACKTERMS = "net.fortytwo.twitlogic.trackTerms",
            SERVER_BASEURI = "net.fortytwo.twitlogic.server.baseURI",
            SERVER_PORT = "net.fortytwo.twitlogic.server.port",
            SERVER_STATICCONTENTDIRECTORY = "net.fortytwo.twitlogic.server.staticContentDirectory",
            TWITTER_CONSUMER_KEY = "net.fortytwo.twitlogic.twitter.consumerKey",
            TWITTER_CONSUMER_SECRET = "net.fortytwo.twitlogic.twitter.consumerSecret",
            TWITTER_ACCESS_TOKEN = "net.fortytwo.twitlogic.twitter.accessToken",
            TWITTER_ACCESS_TOKEN_SECRET = "net.fortytwo.twitlogic.twitter.accessTokenSecret",
            TWITTER_USERNAME = "net.fortytwo.twitlogic.twitter.username",
            TWITTER_PASSWORD = "net.fortytwo.twitlogic.twitter.password",
            XMPP_SERVER = "net.fortytwo.twitlogic.xmpp.server",
            XMPP_PORT = "net.fortytwo.twitlogic.xmpp.port",
            XMPP_REPORTER_USERNAME = "net.fortytwo.twitlogic.xmpp.reporterUsername",
            XMPP_REPORTER_PASSWORD = "net.fortytwo.twitlogic.xmpp.reporterPassword",
            XMPP_REASONER_USERNAME = "net.fortytwo.twitlogic.xmpp.reasonerUsername",
            XMPP_REASONER_PASSWORD = "net.fortytwo.twitlogic.xmpp.reasonerPassword";

    public static final String
            NAMESPACE = "http://fortytwo.net/2009/10/twitlogic#";

    public static final URI
            ASSOCIATION = new URIImpl(NAMESPACE + "Association"),
            SUBJECT = new URIImpl(NAMESPACE + "subject"),
            OBJECT = new URIImpl(NAMESPACE + "object"),
            WORD = new URIImpl(NAMESPACE + "Word"),
            WEIGHT = new URIImpl(NAMESPACE + "weight");

    // Note: keep these in agreement with the ResourceType enum.
    public static final String
            BASE_URI = "http://twitlogic.fortytwo.net/",
            DATASETS_BASEURI = BASE_URI + "dataset/",
            DUMPS_BASEURI = BASE_URI + "dump/",
            GRAPHS_BASEURI = BASE_URI + "graph/",
            HASHTAGS_BASEURI = BASE_URI + "hashtag/",
            LOCATIONS_BASEURI = BASE_URI + "location/",
            MISCELLANEOUS_BASEURI = BASE_URI + "miscellaneous/",
            PERSONS_BASEURI = BASE_URI + "person/",
            TWEETS_BASEURI = BASE_URI + "post/twitter/",
            USERS_BASEURI = BASE_URI + "user/twitter/";

    // This graph contains all of TwitLogic's authoritative metadata about
    // named graphs, microblog posts, microblog authors, etc., as well as
    // voiD metadata about the knowledge base itself.
    public static final URI
            AUTHORITATIVE_GRAPH = null;

    // Other special resources
    public static final String
            TWITLOGIC_DATASET = DATASETS_BASEURI + "twitlogic-full",
            SEMANTICTWEET_DATASET = DATASETS_BASEURI + "semantictweet",
            SEMANTICTWEET_LINKSET1 = DATASETS_BASEURI + "semantictweet-linkset1";

    public static final Pattern
            CONFIG_LIST_PATTERN = Pattern.compile("[A-Za-z0-9-_]+/[A-Za-z0-9-_]+"),
            CONFIG_USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9-_]+");

    private static TypedProperties CONFIGURATION;
    private static final Logger LOGGER;

    static {
        CONFIGURATION = new TypedProperties();

        /*
        try {
            CONFIGURATION.load(TwitLogic.class.getResourceAsStream("twitlogic.properties"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }*/

        InputStream resourceAsStream = TwitLogic.class.getResourceAsStream("logging.properties");

        try {
            LogManager.getLogManager().readConfiguration(resourceAsStream);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER = getLogger(TwitLogic.class);
        LOGGER.info("initialized logging");
    }

    public enum ResourceType {
        DataSet("dataset", "data set"),
        Graph("graph", "named graph"),
        Hashtag("hashtag", "hashtag resource"),
        Location("location", "location"),
        Miscellaneous("miscellaneous", "miscellaneous resource"),
        Person("person", "person"),
        Post("post", "microblog post"),
        User("user", "microblog user account");

        private final String uriPath;
        private final String name;

        private ResourceType(final String uriPath,
                             final String name) {
            this.uriPath = uriPath;
            this.name = name;
        }

        public String getUriPath() {
            return uriPath;
        }

        public String getName() {
            return name;
        }

        public static ResourceType findByName(final String name) {
            for (ResourceType t : ResourceType.values()) {
                if (t.name().equals(name)) {
                    return t;
                }
            }

            return null;
        }
    }

    public static String getName() {
        return "TwitLogic";
    }

    public static String getVersion() {
        return "0.1";
    }

    public static TypedProperties getConfiguration() {
        return CONFIGURATION;
    }

    public static void setConfiguration(final Properties properties) {
        CONFIGURATION = new TypedProperties(properties);
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c.getName());
    }

    public static Set<String> findTrackTerms() throws PropertyException {
        TypedProperties props = TwitLogic.getConfiguration();

        // Note: this doesn't really need to be an order-preserving collection,
        // because Java properties are not order-preserving.
        Set<String> terms = new LinkedHashSet<String>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(TwitLogic.TRACKTERMS)) {
                String listVal = props.getString(key);
                String[] these = listVal.split(",");
                for (String t : these) {
                    t = t.trim();
                    if (0 < t.length()) {
                        terms.add(t);
                    }
                }
            }
        }
        
        return terms;
    }

    // Note: for now, lists are not persisted in any way
    public static Set<User> findFollowList(final TwitterClient client) throws Exception {
        TypedProperties props = TwitLogic.getConfiguration();

        // Note: this doesn't really need to be an order-preserving collection,
        // because Java properties are not order-preserving.
        Set<User> users = new LinkedHashSet<User>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(TwitLogic.FOLLOWLIST)) {
                String listVal = props.getString(key);
                if (!CONFIG_LIST_PATTERN.matcher(listVal).matches()) {
                    throw new PropertyException("invalid list: " + listVal + " (should be of the form user_name/list_id)");
                }

                String[] parts = listVal.split("/");
                User user = new User(parts[0]);
                String listId = parts[1];

                List<User> l = client.getListMembers(user, listId);
                users.addAll(l);
            } else if (key.startsWith(TwitLogic.FOLLOWUSER)) {
                String screenName = props.getString(key);
                if (!CONFIG_USERNAME_PATTERN.matcher(screenName).matches()) {
                    throw new PropertyException("invalid screen name: " + screenName);
                }

                // Twitter requires user IDs (as opposed to screen names) for follow filters.
                User u = client.findUserInfo(screenName);
                users.add(u);
            }
        }

        if (0 == users.size()) {
            throw new Exception("no users to follow!  Set the " + TwitLogic.FOLLOWLIST + " property of your configuration to a non-empty list");
        }
        return users;
    }
}

