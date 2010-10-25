package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.geo.Point;
import net.fortytwo.twitlogic.services.twitter.TwitterAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Sep 3, 2009
 * Time: 10:04:18 PM
 */
public class Tweet implements Resource {
    private static final Logger LOGGER = TwitLogic.getLogger(Tweet.class);

    private User user;

    // private final List<User> contributors;
    private Date createdAt;
    private Entities entities;
    //private final Boolean favorited;
    private Point geo;
    private String id;
    //private User inReplyToUser;
    private Tweet inReplyToTweet;
    //private String newId;
    private Place place;
    // private boolean retweetCount;
    // private boolean retweeted;
    private Tweet retweetOf;
    //private final String source;
    //private Set<...> states;
    private String text;
    //private final Boolean truncated;
    private JSONArray twannotations;

    private final Collection<Triple> annotations = new LinkedList<Triple>();

    /**
     * Creates a new, empty tweet.
     */
    public Tweet() {
    }

    public Tweet(final String id) {
        this.id = id;
    }

    private String stringValue(final String value) {
        return null == value || value.equals("null")
                ? null
                : value;
    }

    /**
     * Parses a tweet in Twitter's status element JSON format.  Some fields are required.
     *
     * @param json a JSON-formatted status element
     * @throws TweetParseException if parsing fails
     */
    public Tweet(final JSONObject json) throws TweetParseException {
        try {
            TwitterAPI.checkJSON(json, TwitterAPI.FieldContext.STATUS);

            JSONObject geoObj = TwitterAPI.getJSONObject(json, TwitterAPI.Field.GEO);
            if (null != geoObj) {
                //LOGGER.info("geo: " + geoObj);
                String type = TwitterAPI.getString(geoObj, TwitterAPI.Field.TYPE);
                if (null == type) {
                    LOGGER.warning("no 'type' for geo object");
                } else if (!type.equals("Point")) {
                    LOGGER.warning("unfamiliar geo type: " + type);
                } else {
                    JSONArray coords = geoObj.getJSONArray(TwitterAPI.Field.COORDINATES.toString());

                    // Note: in the Twitter API 2.0, the order of longitude and latitude will be reversed.
                    geo = new Point(coords.getDouble(1), coords.getDouble(0));
                }

                // TODO: look for unrecognized attributes
            }

            JSONObject placeObj = TwitterAPI.getJSONObject(json, TwitterAPI.Field.PLACE);
            if (null != placeObj) {
                //LOGGER.info("place: " + placeObj);
                place = new Place(placeObj);
            }

            // Run through Twitter's gauntlet of tweet id schemes.
            id = json.optString(TwitterAPI.Field.NEW_ID_STR.toString());
            if (null == id) {
                id = json.optString(TwitterAPI.Field.NEW_ID.toString());
                if (null == id) {
                    id = json.optString(TwitterAPI.Field.ID_STR.toString());
                    if (null == id) {
                        id = json.optString(TwitterAPI.Field.ID.toString());
                    }
                }
            }
            // Note: this has happened at least twice before
            if (null == id) {
                LOGGER.severe("received a tweet without an ID: " + json);
            }

            // Evidently, these three fields are a unit.
            String inReplyToUserId;
            String inReplyToScreenName;
            String inReplyToStatusId = null;

            inReplyToUserId = stringValue(json.optString(TwitterAPI.Field.IN_REPLY_TO_USER_ID_STR.toString()));
            if (null == inReplyToUserId) {
                inReplyToUserId = stringValue(json.optString(TwitterAPI.Field.IN_REPLY_TO_USER_ID.toString()));
            }

            inReplyToScreenName = stringValue(json.optString(TwitterAPI.Field.IN_REPLY_TO_SCREEN_NAME.toString()));

            inReplyToStatusId = stringValue(json.optString(TwitterAPI.Field.IN_REPLY_TO_STATUS_ID_STR.toString()));
            if (null == inReplyToStatusId) {
                inReplyToStatusId = stringValue(json.optString(TwitterAPI.Field.IN_REPLY_TO_STATUS_ID.toString()));
            }

            // Note: a value of "" for inReplyToUserId was observed for a tweet retrieved from a friends list
            if (null != inReplyToUserId && 0 < inReplyToUserId.length()
                    && null != inReplyToScreenName
                    && null != inReplyToStatusId) {
                User u = new User(inReplyToScreenName, Integer.valueOf(inReplyToUserId));
                inReplyToTweet = new Tweet(inReplyToStatusId);
                inReplyToTweet.setUser(u);
            } else {
                inReplyToTweet = null;
            }

            JSONObject rt = json.optJSONObject(TwitterAPI.Field.RETWEETED_STATUS.toString());
            //System.out.println("retweet: " + rt);
            retweetOf = null == rt
                    ? null
                    : new Tweet(rt);

            text = json.optString(TwitterAPI.Field.TEXT.toString());

            // Parse the date provided by Twitter, rather than using the current date/time.
            // We may not have received this tweet in real time.
            String dateString = TwitterAPI.getString(json, TwitterAPI.Field.CREATED_AT);
            try {
                createdAt = TwitterAPI.parseTwitterDateString(dateString);
            } catch (ParseException e) {
                // FIXME: this shouldn't really be a JSONException
                throw new TweetParseException(e);
            }

            JSONObject userJSON = json.getJSONObject(TwitterAPI.Field.USER.toString());
            user = new User(userJSON);

            twannotations = json.optJSONArray(TwitterAPI.Field.ANNOTATIONS.toString());

            JSONObject ent = json.optJSONObject(TwitterAPI.Field.ENTITIES.toString());
            if (null != ent) {
                entities = new Entities(ent);
            }
        } catch (JSONException e) {
            throw new TweetParseException(e);
        }
    }

    public User getUser() {
        return user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Point getGeo() {
        return geo;
    }

    public String getId() {
        return id;
    }

    public Tweet getInReplyToTweet() {
        return inReplyToTweet;
    }

    public Tweet getRetweetOf() {
        return retweetOf;
    }

    public String getText() {
        return text;
    }

    public JSONArray getTwannotations() {
        return twannotations;
    }

    public Resource.Type getType() {
        return Resource.Type.TWEET;
    }

    public Collection<Triple> getAnnotations() {
        return annotations;
    }

    public String toString() {
        return "[tweet #" + id + "]";
    }

    public String describe() {
        return "tweet " + this.getId()
                + (null != this.getGeo() ? (" at \"" + this.getGeo() + "\"") : "")
                + " by @" + this.getUser().getScreenName()
                + ": " + this.getText();
    }

    public boolean equals(final Object other) {
        return other instanceof Tweet
                && id.equals(((Tweet) other).id);
    }

    public int hashCode() {
        return id.hashCode();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setGeo(Point geo) {
        this.geo = geo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setInReplyToTweet(Tweet inReplyToTweet) {
        this.inReplyToTweet = inReplyToTweet;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Entities getEntities() {
        return entities;
    }

    public void setEntities(Entities entities) {
        this.entities = entities;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}

