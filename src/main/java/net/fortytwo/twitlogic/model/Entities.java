package net.fortytwo.twitlogic.model;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.Collection;
import java.util.LinkedList;

import net.fortytwo.twitlogic.services.twitter.TwitterAPI;

/**
 * User: josh
* Date: Aug 26, 2010
* Time: 1:42:33 PM
*/
public class Entities {
    private final Collection<Hashtag> topics = new LinkedList<Hashtag>();
    private final Collection<URIReference> links = new LinkedList<URIReference>();

    public Entities() {
    }

    public Entities(final JSONObject json) throws JSONException {
        JSONArray urls = json.getJSONArray(TwitterAPI.EntitiesField.URLS.toString());
        for (int i = 0; i < urls.length(); i++) {
            links.add(new URIReference(urls.getString(i)));
        }

        JSONArray hashtags = json.getJSONArray(TwitterAPI.EntitiesField.HASHTAGS.toString());
        for (int i = 0; i < hashtags.length(); i++) {
            topics.add(new Hashtag(hashtags.getString(i).toLowerCase()));
        }

        // Note: USER_MENTIONS field is currently not used.
    }

    public Collection<Hashtag> getTopics() {
        return topics;
    }

    public Collection<URIReference> getLinks() {
        return links;
    }
}
