package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.services.twitter.TwitterAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import twitter4j.HashtagEntity;
import twitter4j.URLEntity;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Entities {
    private final Collection<Resource> topics = new LinkedList<Resource>();
    private final Collection<URIReference> links = new LinkedList<URIReference>();

    public Entities() {
    }

    public Entities(final HashtagEntity[] hashtagEntities,
                    final URLEntity[] urlEntities) {
        for (HashtagEntity e : hashtagEntities) {
            topics.add(new Hashtag(e.getText()));
        }

        for (URLEntity e : urlEntities) {
            links.add(new URIReference(e.getURL().toString()));
        }
    }

    public Entities(final JSONObject json) throws JSONException {
        JSONArray urls = json.getJSONArray(TwitterAPI.EntitiesField.URLS.toString());
        for (int i = 0; i < urls.length(); i++) {
            JSONObject e = urls.getJSONObject(i);
            String text = TwitterAPI.getString(e, TwitterAPI.EntitiesField.EXPANDED_URL);
            if (null == text) {
                text = TwitterAPI.getString(e, TwitterAPI.EntitiesField.URL);
            }
            links.add(new URIReference(text));
        }

        JSONArray hashtags = json.getJSONArray(TwitterAPI.EntitiesField.HASHTAGS.toString());
        for (int i = 0; i < hashtags.length(); i++) {
            JSONObject e = hashtags.getJSONObject(i);
            String text = e.getString(TwitterAPI.EntitiesField.TEXT.toString()).toLowerCase();
            topics.add(new Hashtag(text));
        }

        // Note: USER_MENTIONS field is currently not used.
    }

    public Collection<Resource> getTopics() {
        return topics;
    }

    public Collection<URIReference> getLinks() {
        return links;
    }
}
