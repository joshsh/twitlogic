package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.geo.Point;
import net.fortytwo.twitlogic.model.geo.Polygon;
import net.fortytwo.twitlogic.services.twitter.TwitterAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Place {
    private static final Logger LOGGER = TwitLogic.getLogger(Place.class);

    private final JSONObject json;
    private final twitter4j.Place place;

    private String countryCode;
    private String fullName;
    private String name;
    private String url;
    private String id;
    private PlaceType placeType;
    private Collection<Place> containedWithin;

    // Note: this is a simplified data member; Twitter provides a *bounding polygon*
    private Point centroid;

    public Place(final twitter4j.Place p) {
        json = null;
        place = p;

        countryCode = p.getCountryCode();
        fullName = p.getFullName();
        name = p.getName();
        url = p.getURL();
        id = p.getId();
        placeType = PlaceType.lookup(p.getPlaceType());

        containedWithin = new LinkedList<Place>();
        if (null != p.getContainedWithIn()) {
            for (twitter4j.Place other : p.getContainedWithIn()) {
                containedWithin.add(new Place(other));
            }
        }
    }

    public Place(final String id) {
        this.id = id;
        json = null;
        place = null;
    }

    public Place(final JSONObject json) throws TweetParseException {
        this.place = null;
        this.json = json;

        // id is required, as it determines the URI of the place
        try {
            id = json.getString(TwitterAPI.PlaceField.ID.toString());
        } catch (JSONException e) {
            throw new TweetParseException(e);
        }

        countryCode = json.optString(TwitterAPI.PlaceField.COUNTRY_CODE.toString());
        fullName = json.optString(TwitterAPI.PlaceField.FULL_NAME.toString());
        name = json.optString(TwitterAPI.PlaceField.NAME.toString());
        url = json.optString(TwitterAPI.PlaceField.URL.toString());
        String t = json.optString(TwitterAPI.PlaceField.PLACE_TYPE.toString());
        if (null != t) {
            placeType = PlaceType.lookup(t);
            if (null == placeType) {
                LOGGER.warning("unfamiliar place type: '" + t + "'");
            }
        }

        containedWithin = new LinkedList<Place>();
        JSONArray cw = json.optJSONArray(TwitterAPI.PlaceField.CONTAINED_WITHIN.toString());
        if (null != cw) {
            for (int i = 0; i < cw.length(); i++) {
                try {
                    containedWithin.add(new Place(cw.getJSONObject(i)));
                } catch (JSONException e) {
                    throw new TweetParseException(e);
                }
            }
        }

        JSONObject box = json.optJSONObject(TwitterAPI.PlaceField.BOUNDING_BOX.toString());
        if (null != box) {
            JSONArray coords = box.optJSONArray(TwitterAPI.Field.COORDINATES.toString());
            if (null == coords) {
                throw new TweetParseException("no coordinates for bounding box: " + box);
            }

            if (1 != coords.length()) {
                throw new TweetParseException("wrong number of coordinate components for bounding box: " + box);
            }

            Polygon p;
            try {
                p = new Polygon(coords.getJSONArray(0));
            } catch (JSONException e) {
                throw new TweetParseException(e);
            }
            centroid = p.findCentroid();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PlaceType getPlaceType() {
        return placeType;
    }

    public void setPlaceType(PlaceType placeType) {
        this.placeType = placeType;
    }

    public Collection<Place> getContainedWithin() {
        return containedWithin;
    }

    public void setContainedWithin(Collection<Place> containedWithin) {
        this.containedWithin = containedWithin;
    }

    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof Place && ((Place) o).id.equals(id);
    }

    public Point getCentroid() {
        return centroid;
    }

    @Override
    public String toString() {
        if (null != json) {
            return json.toString();
        } else {
            return place.toString();
        }
    }
}
