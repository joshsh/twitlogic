package net.fortytwo.twitlogic.syntax.twannotations;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.TwitLogic;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Jun 5, 2010
 * Time: 4:29:11 PM
 */
public abstract class TwannotationRdfizer implements Handler<JSONObject> {
    private static final Logger LOGGER = TwitLogic.getLogger(TwannotationRdfizer.class);

    protected final Handler<Triple> tripleHandler;
    private final Map<String, TwannotationAttributeRdfizer> attributeRdfizersByFormat;

    public TwannotationRdfizer(final Handler<Triple> tripleHandler) {
        this.tripleHandler = tripleHandler;
        attributeRdfizersByFormat = new HashMap<String, TwannotationAttributeRdfizer>();
    }

    public abstract String getFormat();

    public void registerAttributeRdfizer(final TwannotationAttributeRdfizer rdfizer) {
        TwannotationAttributeRdfizer pre = attributeRdfizersByFormat.get(rdfizer.getFormat());
        if (null != pre) {
            LOGGER.warning("An rdfizer for attribute '"
                    + rdfizer.getFormat() + "' in type '" + getFormat()
                    + "' has already been registered. This new rdfizer replaces it.");
        }

        attributeRdfizersByFormat.put(rdfizer.getFormat(), rdfizer);
    }

    public boolean handle(final JSONObject json) throws HandlerException {
        Iterator keys = json.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();

            TwannotationAttributeRdfizer rdfizer = attributeRdfizersByFormat.get(key);
            if (null == rdfizer) {
                LOGGER.warning("no rdfizer has been registered for type '" + key + "'");
            } else {
                JSONObject obj;
                try {
                    obj = json.getJSONObject(key);
                } catch (JSONException e) {
                    throw new HandlerException(e);
                }
                if (!rdfizer.handle(obj)) {
                    return false;
                }
            }
        }

        return true;
    }
}
