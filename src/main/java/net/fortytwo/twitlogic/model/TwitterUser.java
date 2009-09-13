package net.fortytwo.twitlogic.model;

import org.json.JSONObject;
import org.json.JSONException;

import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.twitter.TwitterAPI;


/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 3, 2009
 * Time: 10:05:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterUser implements Resource {
    //private final Date createdAt;
    //private final String description;
    //private final Integer favoritesCount;
    //private final Integer followersCount;
    //private final Integer friendsCount;
    private final Integer id;
    private final String location;
    private final String name;
    //private final String notifications; // Appropriate data type unknown.
    private final String profileBackgroundColor;
    //private final String profileBackgroundImageUrl;
    //private final Boolean backgroundTile;
    private final String profileImageUrl;
    //private final String profileLinkColor;
    //private final String profileSidebarBorderColor;
    //private final String profileSidebarFillColor;
    private final String profileTextColor;
    private final Boolean isProtected; // Note: field name is "protected"
    private final String screenName;
    //private final Integer statusesCount;
    //private final String timeZone;
    private final String url;
    //private final Integer utcOffset;
    //private final Boolean verified;

    public TwitterUser(final String screenName) {
        this.screenName = screenName;

        id = null;
        location = null;
        name = null;
        profileBackgroundColor = null;
        profileImageUrl = null;
        profileTextColor = null;
        isProtected = null;
        url = null;
    }

    public TwitterUser(final String screenName, final int id) {
        this.id = id;
        this.screenName = screenName;

        location = null;
        name = null;
        profileBackgroundColor = null;
        profileImageUrl = null;
        profileTextColor = null;
        isProtected = null;
        url = null;
    }

    public TwitterUser(final JSONObject json) throws JSONException {
        TwitterAPI.checkJSON(json);

        id = json.getInt(TwitterAPI.Field.ID.toString());
        location = json.getString(TwitterAPI.Field.LOCATION.toString());
        name = json.getString(TwitterAPI.Field.NAME.toString());
        profileBackgroundColor = json.getString(TwitterAPI.Field.PROFILE_BACKGROUND_COLOR.toString());
        profileImageUrl = json.getString(TwitterAPI.Field.PROFILE_IMAGE_URL.toString());
        profileTextColor = json.getString(TwitterAPI.Field.PROFILE_TEXT_COLOR.toString());
        isProtected = json.getBoolean(TwitterAPI.Field.PROTECTED.toString());
        screenName = json.getString(TwitterAPI.Field.SCREEN_NAME.toString());
        url = json.getString(TwitterAPI.Field.URL.toString());
    }

    public Integer getId() {
        return id;
    }

    public String getLocation() {
        return location;
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

    public Boolean isProtected() {
        return isProtected;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getUrl() {
        return url;
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
        if (other instanceof TwitterUser) {
            TwitterUser otherUser = (TwitterUser) other;
            if (null != screenName && null != otherUser.screenName) {
                return screenName.equals(otherUser.screenName);
            } else if (null != id && null != otherUser.id) {
                return id.equals(otherUser.id);
            } else {
                return false;
            }
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
}

/*
        "created_at":"Sat Dec 20 04:35:52 +0000 2008",
        "description":"Writing and Painting are my main two loves. I have a lot of things in my head. A lot that no one gets. They just stare and ask what I've been smoking.",
        "favourites_count":12,
        "followers_count":78,
        "following":null,
        "friends_count":150,
        "id":18261195,
        "location":"Pennsylvania.",
        "name":"Sarah Gyle",
        "notifications":null,
        "profile_background_color":"ffffff",
        "profile_background_image_url":"http:\/\/a1.twimg.com\/profile_background_images\/32524998\/surrealism.jpg",
        "profile_background_tile":true,
        "profile_image_url":"http:\/\/a1.twimg.com\/profile_images\/68846220\/l_cff071ff78d454e3acd0ad5a01ea3a92_normal.jpg",
        "profile_link_color":"dbb963",
        "profile_sidebar_border_color":"782b73",
        "profile_sidebar_fill_color":"8fa374"
        "profile_text_color":"ebaae1",
        "protected":false,
        "screen_name":"MyEgoBeckons",
        "statuses_count":582,
        "time_zone":"Eastern Time (US & Canada)",
        "url":"http:\/\/www.myspace.com\/purpleelephantsarekewl",
        "utc_offset":-18000,
        "verified":false,
*/