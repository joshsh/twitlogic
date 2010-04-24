package edu.rpi.tw.twctwit.query;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.vocabs.DCTerms;
import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.vocabs.RDF;
import net.fortytwo.twitlogic.vocabs.SIOC;
import net.fortytwo.twitlogic.vocabs.SIOCT;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * User: josh
 * Date: Apr 13, 2010
 * Time: 10:13:36 PM
 */
public class SparqlTools {
    private static final String BASE_URI = "http://example.org/bogusBaseURI";

    private static final String
            GOLD_TWEETS_QUERY = "PREFIX rdf: <" + RDF.NAMESPACE + ">" +
            "PREFIX sioc: <" + SIOC.NAMESPACE + ">" +
            "PREFIX sioct: <" + SIOCT.NAMESPACE + ">" +
            "PREFIX dc: <" + DCTerms.NAMESPACE + ">" +
            "PREFIX foaf: <" + FOAF.NAMESPACE + ">" +
            "SELECT ?post ?content ?screen_name ?depiction ?name ?timestamp WHERE {" +
            "?post rdf:type sioct:MicroblogPost ." +
            "?post sioc:content ?content ." +
            "?post sioc:has_creator ?user ." +
            "?user sioc:id ?screen_name ." +
            "?user sioc:account_of ?agent ." +
            "?agent foaf:depiction ?depiction ." +
            "?agent foaf:name ?name ." +
            "?post dc:created ?timestamp ." +
            "?post sioc:topic <http://twitlogic.fortytwo.net/hashtag/gold> ." +
            //"?post sioc:topic ?topic ." +
            "}",
            ISWC_STATEMENTS_QUERY =
                    "SELECT ?p ?o WHERE {" +
                            "<http://twitlogic.fortytwo.net/hashtag/iswc2009> ?p ?o ." +
                            "}";

    private static ParsedQuery parseQuery(final String query) throws MalformedQueryException {
        SPARQLParser parser = new SPARQLParser();
        return parser.parseQuery(query, BASE_URI);

    }

    public static synchronized CloseableIteration<? extends BindingSet, QueryEvaluationException>
    evaluateQuery(final String queryStr,
                  final SailConnection sc) throws QueryException {
        ParsedQuery query = null;
        try {
            query = parseQuery(queryStr);
        } catch (MalformedQueryException e) {
            throw new QueryException(e);
        }

        MapBindingSet bindings = new MapBindingSet();
        boolean includeInferred = false;
        try {
            return sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, includeInferred);
        } catch (SailException e) {
            throw new QueryException(e);
        }
    }

    public static void queryAndWriteJSON(final String queryStr,
                                         final SailConnection sc,
                                         final OutputStream out,
                                         final int limit) throws QueryException {
        SPARQLResultsJSONWriter w = new SPARQLResultsJSONWriter(out);
        List<String> columnHeaders = new LinkedList<String>();
        // FIXME: *do* specify the column headers
        //columnHeaders.add("post");
        //columnHeaders.add("content");
        //columnHeaders.add("screen_name");
        try {
            w.startQueryResult(columnHeaders);
        } catch (TupleQueryResultHandlerException e) {
            throw new QueryException(e);
        }

        CloseableIteration<? extends BindingSet, QueryEvaluationException> iter
                = evaluateQuery(queryStr, sc);
        int count = 0;
        try {
            try {
                while (iter.hasNext() && count < limit) {
                    w.handleSolution(iter.next());
                    count++;
                }
            } finally {
                iter.close();
            }

            w.endQueryResult();
        } catch (QueryEvaluationException e) {
            throw new QueryException(e);
        } catch (TupleQueryResultHandlerException e) {
            throw new QueryException(e);
        }
    }

    public static void main(final String[] args) throws Exception {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("/Users/josh/projects/fortytwo/twitlogic/config/twitlogic.properties"));
            TwitLogic.setConfiguration(props);

            // Create a persistent store.
            TweetStore store = new TweetStore();
            try {
                store.initialize();

                TweetStoreConnection c = store.createConnection();
                try {
                    SailConnection sc = c.getSailConnection();

                    queryAndWriteJSON(GOLD_TWEETS_QUERY, sc, System.out, 100);
                    //queryAndWriteJSON(ISWC_STATEMENTS_QUERY, sc, System.out);
                } finally {
                    c.close();
                }
            } finally {
                store.shutDown();
            }
        }

        catch (Throwable t)

        {
            t.printStackTrace();
        }
    }
}
