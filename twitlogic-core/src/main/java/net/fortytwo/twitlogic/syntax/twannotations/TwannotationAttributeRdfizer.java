package net.fortytwo.twitlogic.syntax.twannotations;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Triple;
import org.json.JSONObject;

/**
 * Note: with the current API, an attribute parser has no awareness of any other attributes in an annotation.
 * This may need to be changed, as it is foreseeable that certain attributes are only meaningful in combination with others.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class TwannotationAttributeRdfizer implements Handler<JSONObject> {
    protected final Handler<Triple> tripleHandler;

    public TwannotationAttributeRdfizer(final Handler<Triple> tripleHandler) {
        this.tripleHandler = tripleHandler;
    }

    public abstract String getFormat();
}
