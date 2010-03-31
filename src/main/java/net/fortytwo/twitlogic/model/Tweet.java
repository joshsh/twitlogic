package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.twitter.TwitterAPI;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;
import java.util.Date;

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
    private User inReplyToUser;
    private Tweet inReplyToTweet;
    // private final ??? place;
    private Tweet retweetOf;
    //private final String source;
    private String text;
    //private final Boolean truncated;

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

        String g = User.getString(json, TwitterAPI.Field.GEO);
        geo = g;
        if (null != geo) {
            LOGGER.info("geo: " + geo);
        }
        id = json.getString(TwitterAPI.Field.ID.toString());

        String inReplyToUserId = stringValue(json.getString(TwitterAPI.Field.IN_REPLY_TO_USER_ID.toString()));
        String inReplyToScreenName = stringValue(json.getString(TwitterAPI.Field.IN_REPLY_TO_SCREEN_NAME.toString()));
        inReplyToUser = (null != inReplyToUserId)
                ? new User(inReplyToScreenName, Integer.valueOf(inReplyToUserId))
                : (null != inReplyToScreenName)
                ? new User(inReplyToScreenName)
                : null;

        String inReplyToStatusId = json.getString(TwitterAPI.Field.IN_REPLY_TO_STATUS_ID.toString());
        inReplyToTweet = null == inReplyToStatusId
                ? null
                : new Tweet(inReplyToStatusId);

        JSONObject rt = json.optJSONObject(TwitterAPI.Field.RETWEETED_STATUS.toString());
        retweetOf = null == rt
                ? null
                : new Tweet(rt);

        text = json.getString(TwitterAPI.Field.TEXT.toString());

        String dateString = User.getString(json, TwitterAPI.Field.CREATED_AT);
        // TODO: use the date read in from Twitter, rather than the current time
        createdAt = new Date();

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

    public User getInReplyToUser() {
        return inReplyToUser;
    }

    public String getText() {
        return text;
    }

    public Resource.Type getType() {
        return Resource.Type.TWEET;
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

    public void setInReplyToUser(User inReplyToUser) {
        this.inReplyToUser = inReplyToUser;
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
