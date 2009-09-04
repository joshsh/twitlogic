package net.fortytwo.twitlogic.twittertools;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 3, 2009
 * Time: 10:07:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterAPI {
    public enum Field {
        CREATED_AT("created_at"), // Note: used in multiple contexts
        DECRIPTION("description"),
        FAVORITED("favorited"),
        FAVORITES_COUNT("favourites_count"),
        FOLLOWERS_COUNT("followers_count"),
        FOLLOWING("following"),
        FRIENDS_COUNT("friends_count"),
        ID("id"), // Note: used in multiple contexts
        IN_REPLY_TO_SCREEN_NAME("in_reply_to_screen_name"),
        IN_REPLY_TO_STATUS_ID("in_reply_to_status_id"),
        IN_REPLY_TO_USER_ID("in_reply_to_user_id"),
        LOCATION("location"),
        NAME("name"),
        NOTIFICATIONS("notifications"),
        PROFILE_BACKGROUND_COLOR("profile_background_color"),
        PROFILE_BACKGROUND_IMAGE_URL("profile_background_image_url"),
        PROFILE_BACKGROUND_TILE("profile_background_tile"),
        PROFILE_IMAGE_URL("profile_image_url"),
        PROFILE_LINK_COLOR("profile_link_color"),
        PROFILE_SIDEBAR_BORDER_COLOR("profile_sidebar_border_color"),
        PROFILE_SIDEBAR_COLOR("profile_sidebar_fill_color"),
        PROFILE_TEXT_COLOR("profile_text_color"),
        PROTECTED("protected"),
        SCREEN_NAME("screen_name"),
        SOURCE("source"),
        STATUSES_COUNT("statuses_count"),
        TEXT("text"),
        TIME_ZONE("time_zone"),
        TRUNCATED("truncated"),
        URL("url"),
        USER("user"),
        UTC_OFFSET("utc_offset"),
        VERIFIED("verified");

        private static final Map<String, Field> fieldsByName;

        static {
            fieldsByName = new HashMap<String, Field>();
            for (Field f : Field.values()) {
                fieldsByName.put(f.name, f);
            }
        }

        private final String name;

        private Field(final String name) {
            this.name = name;
        }

        public static Field valueByName(final String name) {
            return fieldsByName.get(name);
        }

        public String toString() {
            return name;
        }
    }

    public static void main(final String[] args) {
        Field f = Field.valueByName("verified");
        System.out.println("f = " + f);
    }
}
