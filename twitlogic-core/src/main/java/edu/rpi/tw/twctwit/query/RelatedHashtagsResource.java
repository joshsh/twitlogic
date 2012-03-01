package edu.rpi.tw.twctwit.query;

import net.fortytwo.flow.rdf.ranking.HandlerException;
import net.fortytwo.sesametools.ldserver.query.QueryResource;
import net.fortytwo.sesametools.mappingsail.MappingSail;
import org.json.JSONArray;
import org.json.JSONException;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import java.util.Collection;
import java.util.Map;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RelatedHashtagsResource extends QueryResource {
    private static final String
            RESOURCE_PARAM = "resource",
            STEPS_PARAM = "steps",
            LIMIT_PARAM = "limit";

    private static final int
            DEFAULT_STEPS = 500,
            MAX_STEPS = 5000,
            DEFAULT_LIMIT = 10,
            MAX_LIMIT = 100;

    @Override
    public void handle(final Request request,
                       final Response response) {

        Map<String, String> args = getArguments(request);

        //try {

        String s;
        s = args.get(LIMIT_PARAM);
        int limit;
        if (null == s) {
            limit = DEFAULT_LIMIT;
        } else {
            limit = Integer.valueOf(s);
            if (limit < 1) {
                limit = 1;
            } else if (limit > MAX_LIMIT) {
                limit = MAX_LIMIT;
            }
        }
        s = args.get(STEPS_PARAM);
        int steps;
        if (null == s) {
            steps = DEFAULT_STEPS;
        } else {
            steps = Integer.valueOf(s);
            if (steps < 1) {
                steps = 1;
            } else if (steps > MAX_STEPS) {
                steps = MAX_STEPS;
            }
        }

        String resource = args.get(RESOURCE_PARAM);
        if (null == resource) {
            throw new IllegalArgumentException("missing '" + RESOURCE_PARAM + "' parameter");
        }

        JSONArray a = null;
        try {
            a = relatedTagsJSON(new URIImpl(resource), limit, steps);
        } catch (SailException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
        } catch (HandlerException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
        } catch (JSONException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
        }
        //System.out.println("query = " + query);

        // TODO
        response.setEntity(new StringRepresentation(a.toString(), MediaType.APPLICATION_JSON));
    }

    private JSONArray relatedTagsJSON(final Resource resource,
                                      final int limit,
                                      final int steps) throws SailException, HandlerException, JSONException {
        Sail baseSail = sail instanceof MappingSail
                ? ((MappingSail) sail).getBaseSail()
                : sail;

        Collection<Resource> results;

        SailConnection sc = baseSail.getConnection();
        try {
            //System.out.println("resource (really): " + resource);
            RelatedHashtagsInferencer inf = new RelatedHashtagsInferencer(sc, resource);
            int used = inf.compute(steps);

            results = inf.currentHashtagResults(limit);

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
}