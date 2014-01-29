package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.services.twitter.TwitterAPI;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;


/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class User implements Resource {
    //private final Boolean contributorsEnabled;
    //private final Date createdAt;
    //private final boolean defaultProfile;
    //private final boolean defaultProfileImage;
    private final String description;
    //private final boolean discoverableByEmail;
    //private final boolean discoverableByMobilePhone;
    //private final String displayUrl;
    //private final boolean emailDiscoverabilitySet;
    //private final String expandedUrl;
    //private final Integer favoritesCount;
    //private final Integer followersCount;
    //private final Integer followRequestSent;
    //private final Integer friendsCount;
    private final boolean geoEnabled;
    private final Long id;
    //private final boolean isTranslator;  // TODO: what is the significance of this field?
    //private final String language;
    //private final String listedCount;
    private final String location;
    //private final boolean mobilePhoneDiscoverabilitySet;
    private final String name;
    //private final String notifications; // Appropriate data type unknown.
    private final String profileBackgroundColor;
    //private final String profileBackgroundImageUrl;
    //private final String profileBackgroundImageUrlHttps;
    //private final Boolean backgroundTile;
    private final String profileImageUrl;
    //private final String profileImageUrlHttps;
    //private final String profileLinkColor;
    //private final String profileSidebarBorderColor;
    //private final String profileSidebarFillColor;
    private final String profileTextColor;
    //private final boolean? profileUseBackgroundImage;
    //private final Boolean isProtected; // Note: field name is "protected"
    private final String screenName;
    //private final boolean showAllInlineMedia;
    //private final Tweet status;
    //private final Integer statusesCount;
    //private final String timeZone;
    private final String url;
    //private final Integer utcOffset;
    //private final Boolean verified;

    private final Person heldBy;

    private Collection<User> followers;
    private Collection<User> followees;

    public User(final twitter4j.User u) {
        //System.out.println("user: " + u);
        description = u.getDescription();
        geoEnabled = u.isGeoEnabled();
        id = u.getId();
        location = u.getLocation();
        name = u.getName();
        profileBackgroundColor = u.getProfileBackgroundColor();
        profileImageUrl = u.getProfileImageURL().toString();
        profileTextColor = u.getProfileTextColor();
        screenName = u.getScreenName();
        url = null == u.getURL() ? null : u.getURL().toString();

        heldBy = new Person(this);

        validate();
    }

    public User(final String screenName) {
        this.screenName = screenName;

        id = null;
        geoEnabled = false;
        location = null;
        description = null;
        name = null;
        profileBackgroundColor = null;
        profileImageUrl = null;
        profileTextColor = null;
        //isProtected = null;
        url = null;

        heldBy = new Person(this);

        validate();
    }

    public User(final long id) {
        this.id = id;

        screenName = null;
        geoEnabled = false;
        location = null;
        description = null;
        name = null;
        profileBackgroundColor = null;
        profileImageUrl = null;
        profileTextColor = null;
        //isProtected = null;
        url = null;

        heldBy = new Person(this);

        validate();
    }

    public User(final String screenName, final long id) {
        this.id = id;
        this.screenName = screenName;

        geoEnabled = false;
        location = null;
        description = null;
        name = null;
        profileBackgroundColor = null;
        profileImageUrl = null;
        profileTextColor = null;
        //isProtected = null;
        url = null;

        heldBy = new Person(this);

        validate();
    }

    public User(final JSONObject json) throws TweetParseException {
        try {
            TwitterAPI.checkJSON(json, TwitterAPI.FieldContext.USER);

            Long id0 = json.getLong(TwitterAPI.Field.ID.toString());
            if (null == id0) {
                id0 = Long.valueOf(json.getString(TwitterAPI.Field.ID_STR.toString()));
            }
            id = id0;

            geoEnabled = json.getBoolean(TwitterAPI.Field.GEO_ENABLED.toString());
            location = TwitterAPI.getString(json, TwitterAPI.Field.LOCATION);
            description = TwitterAPI.getString(json, TwitterAPI.Field.DESCRIPTION);
            name = TwitterAPI.getString(json, TwitterAPI.Field.NAME);
            profileBackgroundColor = TwitterAPI.getString(json, TwitterAPI.Field.PROFILE_BACKGROUND_COLOR);
            profileImageUrl = TwitterAPI.getString(json, TwitterAPI.Field.PROFILE_IMAGE_URL);
            profileTextColor = TwitterAPI.getString(json, TwitterAPI.Field.PROFILE_TEXT_COLOR);

            // Note: this field has been disabled in TwitLogic, as it is currently not used and because of some strange
            // JSON from Twitter: in at least one status element received from the Streaming API, a value of "null" was
            // given, instead of "true" or "false".
            //isProtected = json.getBoolean(TwitterAPI.Field.PROTECTED.toString());

            screenName = TwitterAPI.getString(json, TwitterAPI.Field.SCREEN_NAME);
            url = TwitterAPI.getString(json, TwitterAPI.Field.URL);

            heldBy = new Person(this);
        } catch (JSONException e) {
            throw new TweetParseException(e);
        }

        validate();
    }

    public boolean getGeoEnabled() {
        return geoEnabled;
    }

    public Long getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getProfileBackgroundColor() {
        return profileBackgroundColor;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getProfileTextColor() {
        return profileTextColor;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getUrl() {
        return url;
    }

    public Person getHeldBy() {
        return heldBy;
    }

    public Collection<User> getFollowers() {
        return followers;
    }

    public Collection<User> getFollowees() {
        return followees;
    }

    public void setFollowers(final Collection<User> followers) {
        this.followers = followers;
    }

    public void setFollowees(final Collection<User> followees) {
        this.followees = followees;
    }

    public String toString() {
        return null != screenName
                ? "@" + screenName
                : null != id
                ? "[user " + id + "]"
                : "[unidentified user]";
    }

    public Type getType() {
        return Type.USER;
    }

    public boolean equals(final Object other) {
        if (other instanceof User) {
            User otherUser = (User) other;
            if (null != screenName && null != otherUser.screenName) {
                return screenName.equals(otherUser.screenName);
            } else return null != id && null != otherUser.id && id.equals(otherUser.id);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return null != screenName
                ? screenName.hashCode()
                : null != id
                ? id.hashCode()
                : 0;
    }

    private void validate() {
        if (null != id && id <= 0) {
            throw new IllegalArgumentException("invalid user id");
        }
    }
}
