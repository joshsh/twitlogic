package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.twitter.TwitterAPI;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.text.ParseException;

/**
 * User: josh
 * Date: Sep 3, 2009
 * Time: 10:04:18 PM
 */
public class Tweet implements Resource {
    private static final Logger LOGGER = TwitLogic.getLogger(Tweet.class);

    private User user;

    // private final List<User> contributors;
    // private final ??? coordinates;
    private Date createdAt;
    //private final Boolean favorited;
    private String geo;
    private String id;
    //private User inReplyToUser;
    private Tweet inReplyToTweet;
    // private final ??? place;
    private Tweet retweetOf;
    //private final String source;
    private String text;
    //private final Boolean truncated;

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
     * @param json
     * @throws JSONException
     */
    public Tweet(final JSONObject json) throws JSONException {
        TwitterAPI.checkJSON(json, TwitterAPI.FieldContext.STATUS);

        geo = User.getString(json, TwitterAPI.Field.GEO);
        if (null != geo) {
            LOGGER.info("geo: " + geo);
        }
        id = json.getString(TwitterAPI.Field.ID.toString());

        // Evidently, these three fields are a unit.
        String inReplyToUserId = stringValue(json.getString(TwitterAPI.Field.IN_REPLY_TO_USER_ID.toString()));
        String inReplyToScreenName = stringValue(json.getString(TwitterAPI.Field.IN_REPLY_TO_SCREEN_NAME.toString()));
        String inReplyToStatusId = stringValue(json.getString(TwitterAPI.Field.IN_REPLY_TO_STATUS_ID.toString()));
        if (null != inReplyToUserId
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

        text = json.getString(TwitterAPI.Field.TEXT.toString());

        // Parse the date provided by Twitter, rather than using the current date/time.
        // We may not have received this tweet in real time.
        String dateString = User.getString(json, TwitterAPI.Field.CREATED_AT);
        try {
            createdAt = TwitterAPI.parseTwitterDateString(dateString);
        } catch (ParseException e) {
            // FIXME: this shouldn't really be a JSONException
            throw new JSONException(e);
        }

        JSONObject userJSON = json.getJSONObject(TwitterAPI.Field.USER.toString());
        user = new User(userJSON);
    }

    public User getUser() {
        return user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getGeo() {
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

    public Resource.Type getType() {
        return Resource.Type.TWEET;
    }

    public Collection<Triple> getAnnotations() {
        return annotations;
    }

    public String toString() {
        return "[tweet #" + id + "]";
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

    public void setGeo(String geo) {
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
}

/*
    "created_at":"Thu Sep 03 22:58:51 +0000 2009",
    "favorited":false,
    "id":3744161500,
    "in_reply_to_screen_name":"hannahmendonsa",
    "in_reply_to_status_id":3743565780,
    "in_reply_to_user_id":70044174,
    "source":"web"
    "text":"@hannahmendonsa I wanna see a kid who looks like Maika!\nI don't have any of those, haha. I've got like two that look like Brendon Urie.",
    "truncated":false,
 */
