package edu.rpi.tw.twctwit.query;

import edu.rpi.tw.patadata.PataException;
import net.fortytwo.twitlogic.server.rewriter.RewriterSail;
import org.json.JSONArray;
import org.json.JSONException;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.util.Collection;

/**
 * User: josh
 * Date: Apr 18, 2010
 * Time: 2:00:45 PM
 */
public class RelatedHashtagsResource extends QueryResource {
    private static final String
            RESOURCE_PARAM = "resource";

    private final Representation result;

    public RelatedHashtagsResource(final Context context,
                                       final Request request,
                                       final Response response) throws Throwable {
        super(context, request, response);

        //try {
        String resource = arguments.get(RESOURCE_PARAM);

        if (null == resource) {
            throw new IllegalArgumentException("missing '" + RESOURCE_PARAM + "' parameter");
        }

        JSONArray a = relatedTagsJSON(new URIImpl(resource));
        //System.out.println("query = " + query);

        //System.out.println("result: " + a);

        // TODO
        result = new StringRepresentation(a.toString(), MediaType.APPLICATION_JSON);
        //} catch (Throwable t) {
        //    t.printStackTrace();
        //    throw t;
        //}
    }

    private JSONArray relatedTagsJSON(final Resource resource) throws SailException, PataException, JSONException {
        Sail baseSail = sail instanceof RewriterSail
                ? ((RewriterSail) sail).getBaseSail()
                : sail;

        Collection<Resource> results;

        SailConnection sc = baseSail.getConnection();
        try {
            //System.out.println("resource (really): " + resource);
            RelatedHashtagsInferencer inf = new RelatedHashtagsInferencer(sc, resource);
            int steps = 500;
            int used = inf.compute(steps);

            results = inf.currentHashtagResults(10);

        } finally {
            sc.close();
        }
        /*
        System.out.println("" + used + " of " + steps + " cycles used.  Results:");
        for (Resource r : results) {
            System.out.println("\t" + r);
        }//*/

        JSONArray a = new JSONArray();

        for (Resource r : results) {
            //JSONObject o = new JSONObject();
            //o.put("resource", r.toString());
            //o.put("label", "#" + ((URI) r).getLocalName());
            a.put(r.toString());
        }

        return a;
    }

    @Override
    public Representation represent(final Variant variant) {
        return result;
    }
}