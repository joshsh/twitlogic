package edu.rpi.tw.twctwit.query;

import net.fortytwo.twitlogic.TwitLogic;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.logging.Logger;

/**
 * Information and non-information resources are distinguished by the suffix of the resource's URI:
 * 1) information resource URIs end in .rdf or .trig
 * 2) non-information resources have no such suffix (and TwitLogic will not make statements about such URIs)
 * <p/>
 * A request for an information resource is fulfilled with the resource itself.  No content negotiation occurs.
 * <p/>
 * A request for a non-information resource is fulfilled with a 303-redirect to an information resource of the appropriate media type.
 * <p/>
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:55:27 PM
 */
public class SparqlResource extends QueryResource {

    private static final Logger LOGGER
            = TwitLogic.getLogger(SparqlResource.class);

    private final String query;

    public SparqlResource(final Context context,
                          final Request request,
                          final Response response) throws Exception {
        super(context, request, response);

        query = arguments.get("query");
        if (null == query) {
            throw new IllegalArgumentException("no query argument specified");
        }

        String output = arguments.get("output");

        // Humor those clients which use an "output" argument, instead of content
        // negotation, to specify an output format.
        if (null != output) {
            if (output.equals("json")) {
                getVariants().add(new Variant(SparqlTools.SparqlResultFormat.JSON.getMediaType()));
            } else if (output.equals("xml")) {
                getVariants().add(new Variant(SparqlTools.SparqlResultFormat.XML.getMediaType()));
            } else {
                throw new IllegalArgumentException("bad value for 'output' parameter: " + output);
            }
        } else {
            getVariants().addAll(SparqlTools.SparqlResultFormat.getVariants());
        }
    }

    @Override
    public Representation represent(final Variant variant) {
        return new SparqlQueryRepresentation(query, sail, readLimit(), variant.getMediaType());
    }
}