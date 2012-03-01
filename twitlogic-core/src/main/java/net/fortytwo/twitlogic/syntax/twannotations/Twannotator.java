package net.fortytwo.twitlogic.syntax.twannotations;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.TwitLogic;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Twannotator implements Handler<Tweet> {
    private static final Logger LOGGER = TwitLogic.getLogger(Twannotator.class);

    private final Map<String, TwannotationRdfizer> rdfizersByFormat;

    public Twannotator() {
         rdfizersByFormat = new HashMap<String, TwannotationRdfizer>();
    }

    public void registerTwannotationRdfizer(final TwannotationRdfizer rdfizer) {
         TwannotationRdfizer pre = rdfizersByFormat.get(rdfizer.getFormat());
        if (null != pre) {
            LOGGER.warning("An rdfizer for type '"
                    + rdfizer.getFormat()
                    + "' has already been registered. This new rdfizer replaces it.");
        }

        rdfizersByFormat.put(rdfizer.getFormat(), rdfizer);
    }

    public boolean handle(final Tweet tweet) throws HandlerException {
        try {
            if (null != tweet.getTwannotations()) {
                for (int i = 0; i < tweet.getTwannotations().length(); i++) {
                    JSONObject json = tweet.getTwannotations().getJSONObject(i);
                    String key = (String) json.keys().next();

                    TwannotationRdfizer rdfizer = rdfizersByFormat.get(key);
                    if (null == rdfizer) {
                        LOGGER.warning("no rdfizer has been registered for type '" + key + "'");
                    } else {
                        JSONObject obj = json.getJSONObject(key);
                        if (!rdfizer.handle(obj)) {
                            return false;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new HandlerException(e);
        }

        return true;
    }
}
