package edu.rpi.tw.twctwit.query;

import edu.rpi.tw.patadata.PataException;
import net.fortytwo.twitlogic.server.rewriter.RewriterSail;
import net.fortytwo.twitlogic.vocabs.DCTerms;
import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.vocabs.RDF;
import net.fortytwo.twitlogic.vocabs.SIOC;
import net.fortytwo.twitlogic.vocabs.SIOCT;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.Collection;

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
            ALTERNATIVE_TOPICS_PLACEHOLDER = "INSERT TOPIC ALTERNATIVES HERE",
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
    private static final String

            TWEETS_WITH_ALTERNATIVE_TOPICS_QUERY = "PREFIX rdf: <" + RDF.NAMESPACE + ">\n" +
            "PREFIX sioc: <" + SIOC.NAMESPACE + ">\n" +
            "PREFIX sioct: <" + SIOCT.NAMESPACE + ">\n" +
            "PREFIX dc: <" + DCTerms.NAMESPACE + ">\n" +
            "PREFIX foaf: <" + FOAF.NAMESPACE + ">\n" +
            "PREFIX xsd: <" + XMLSchema.NAMESPACE + ">\n" +
            "SELECT ?post ?content ?screen_name ?depiction ?name ?timestamp WHERE {\n" +
            "  ?post rdf:type sioct:MicroblogPost .\n" +
            "  ?post sioc:content ?content .\n" +
            "  ?post sioc:has_creator ?user .\n" +
            "  ?user sioc:id ?screen_name .\n" +
            "  ?user sioc:account_of ?agent .\n" +
            "  ?agent foaf:depiction ?depiction .\n" +
            "  ?agent foaf:name ?name .\n" +
            "  ?post dc:created ?timestamp .\n" +
            ALTERNATIVE_TOPICS_PLACEHOLDER +
            "  FILTER ( ?timestamp > xsd:dateTime(\"" + MIN_TIMESTAMP_PLACEHOLDER + "\") )\n" +
            "}\n" +
            "ORDER BY DESC ( ?timestamp )";

    private final Representation result;

    public RelatedTweetsResource(final Context context,
                                 final Request request,
                                 final Response response) throws Throwable {
        super(context, request, response);

        //try {
            String resource = arguments.get(RESOURCE_PARAM);

            String after = readAfter();

            //System.out.println("resource: " + resource);
            //System.out.println("after: " + after);

            if (null == resource) {
                throw new IllegalArgumentException("missing '" + RESOURCE_PARAM + "' parameter");
            }

            /*String query = TWEETS_WITH_TOPIC_QUERY
           .replace(TOPIC_PLACEHOLDER, resource)
           .replace(MIN_TIMESTAMP_PLACEHOLDER, after);*/
            String query = alternativesQuery(new URIImpl(resource), after);
            //System.out.println("query = " + query);

            result = new SparqlQueryRepresentation(query, sail, readLimit());
        //} catch (Throwable t) {
        //    t.printStackTrace();
        //    throw t;
        //}
    }

    private String alternativesQuery(final Resource resource,
                                     final String after) throws SailException, PataException {
        Sail baseSail = sail instanceof RewriterSail
                ? ((RewriterSail) sail).getBaseSail()
                : sail;

        //SailConnection sc = sail.getConnection();
        SailConnection sc = baseSail.getConnection();
        try {
            //System.out.println("resource (really): " + resource);
            ConferenceInferencer inf = new ConferenceInferencer(sc, resource);
            int steps = 150;
            int used = inf.compute(steps);

            Collection<Resource> results = inf.currentHashtagResults();

            /*
            System.out.println("" + used + " of " + steps + " cycles used.  Results:");
            for (Resource r : results) {
                System.out.println("\t" + r);
            }//*/

            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Resource r : results) {
                if (first) {
                    first = false;
                } else {
                    sb.append("    UNION\n");
                }

                sb.append("  { ?post sioc:topic <").append(r).append("> }\n");
            }

            return TWEETS_WITH_ALTERNATIVE_TOPICS_QUERY
                    .replace(ALTERNATIVE_TOPICS_PLACEHOLDER, sb.toString())
                    .replace(MIN_TIMESTAMP_PLACEHOLDER, after);
        } finally {
            sc.close();
        }
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
