package edu.rpi.tw.twctwit.query;

import net.fortytwo.twitlogic.vocabs.DCTerms;
import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.vocabs.RDF;
import net.fortytwo.twitlogic.vocabs.SIOC;
import net.fortytwo.twitlogic.vocabs.SIOCT;
import org.openrdf.model.vocabulary.XMLSchema;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * User: josh
 * Date: Apr 18, 2010
 * Time: 2:00:45 PM
 */
public class RelatedTweetsResource extends QueryResource {
    private static final String
            RESOURCE_PARAM = "resource",
            AFTER_PARAM = "after";

    private static final String
            TOPIC_PLACEHOLDER = "INSERT_TOPIC_HERE",
            MIN_TIMESTAMP_PLACEHOLDER = "INSERT_MIN_TIMESTAMP_HERE";
    private static final String
            DEFAULT_MIN_TIMESTAMP = "2000-01-01T00:00:00Z";

    private static final String
            TWEETS_WITH_TOPIC_QUERY = "PREFIX rdf: <" + RDF.NAMESPACE + ">" +
            "PREFIX sioc: <" + SIOC.NAMESPACE + ">" +
            "PREFIX sioct: <" + SIOCT.NAMESPACE + ">" +
            "PREFIX dc: <" + DCTerms.NAMESPACE + ">" +
            "PREFIX foaf: <" + FOAF.NAMESPACE + ">" +
            "PREFIX xsd: <" + XMLSchema.NAMESPACE + ">" +
            "SELECT ?post ?content ?screen_name ?depiction ?name ?timestamp WHERE {" +
            "  ?post rdf:type sioct:MicroblogPost ." +
            "  ?post sioc:content ?content ." +
            "  ?post sioc:has_creator ?user ." +
            "  ?user sioc:id ?screen_name ." +
            "  ?user sioc:account_of ?agent ." +
            "  ?agent foaf:depiction ?depiction ." +
            "  ?agent foaf:name ?name ." +
            "  ?post dc:created ?timestamp ." +
            "  ?post sioc:topic <" + TOPIC_PLACEHOLDER + "> ." +
            "  FILTER ( ?timestamp > xsd:dateTime(\"" + MIN_TIMESTAMP_PLACEHOLDER + "\") )" +
            "}" +
            "ORDER BY DESC ( ?timestamp )";

    private final Representation result;

    public RelatedTweetsResource(final Context context,
                                 final Request request,
                                 final Response response) throws Exception {
        super(context, request, response);

        String resource = arguments.get(RESOURCE_PARAM);

        String after = readAfter();

        //System.out.println("resource: " + resource);
        //System.out.println("after: " + after);

        if (null == resource) {
            throw new IllegalArgumentException("missing '" + RESOURCE_PARAM + "' parameter");
        }

        String query = TWEETS_WITH_TOPIC_QUERY
                .replace(TOPIC_PLACEHOLDER, resource)
                .replace(MIN_TIMESTAMP_PLACEHOLDER, after);

        result = new SparqlQueryRepresentation(query, sail, readLimit());
    }

    // TODO: bad timestamps could cause SPARQL evaluation errors. It may be better to catch them at a higher level.
    private String readAfter() {
        String after = arguments.get(AFTER_PARAM);
        if (null == after) {
            after = DEFAULT_MIN_TIMESTAMP;
        }

        return after;
    }

    @Override
    public Representation represent(final Variant variant) {
        return result;
    }
}
