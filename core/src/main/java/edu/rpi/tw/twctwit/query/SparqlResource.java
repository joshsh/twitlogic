package edu.rpi.tw.twctwit.query;

import net.fortytwo.twitlogic.TwitLogic;
import org.openrdf.sail.SailException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import java.util.logging.Logger;

/*
wget "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010"

wget --header "Accept: application/sparql-results+json" "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010"

wget --header "Accept: application/sparql-results+xml" "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010"

wget "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010&output=json"

wget "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010&output=xml"
 */

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
    private Representation representation;

    public SparqlResource(final Context context,
                          final Request request,
                          final Response response) throws Exception {
        super(context, request, response);

        try {
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
        } catch (Throwable t) {
            // TODO: use logging instead
            t.printStackTrace(System.err);
            throw new Exception(t);
        }
    }

    @Override
    public Representation represent(final Variant variant) throws ResourceException {
        try {
            return new SparqlQueryRepresentation(query, sail, readLimit(), variant.getMediaType());
        } catch (QueryException e) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
        } catch (SailException e) {
            // TODO: use logging instead
            e.printStackTrace(System.err);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        } catch (Throwable e) {
            // TODO: use logging instead
            e.printStackTrace(System.err);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }
}