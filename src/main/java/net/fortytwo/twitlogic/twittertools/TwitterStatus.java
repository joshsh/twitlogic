package net.fortytwo.twitlogic.twittertools;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 3, 2009
 * Time: 10:04:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterStatus {
    private final TwitterUser user;

    //private final Date createdAt;
    //private final Boolean favorited;
    private final String id;
    private final String inReplyToScreenName;
    private final String inReplyToStatusId;
    private final String inReplyToUserId;
    //private final String source;
    private final String text;
    //private final Boolean truncated;

    public TwitterStatus(final JSONObject json) throws JSONException {
        id = json.getString(TwitterAPI.Field.ID.toString());
        inReplyToScreenName = json.getString(TwitterAPI.Field.IN_REPLY_TO_SCREEN_NAME.toString());
        inReplyToStatusId = json.getString(TwitterAPI.Field.IN_REPLY_TO_STATUS_ID.toString());
        inReplyToUserId = json.getString(TwitterAPI.Field.IN_REPLY_TO_USER_ID.toString());
        text = json.getString(TwitterAPI.Field.TEXT.toString());

        JSONObject userJSON = json.getJSONObject(TwitterAPI.Field.USER.toString());
        user = new TwitterUser(userJSON);
    }

    public TwitterUser getUser() {
        return user;
    }

    public String getId() {
        return id;
    }

    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }

    public String getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public String getInReplyToUserId() {
        return inReplyToUserId;
    }

    public String getText() {
        return text;
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
