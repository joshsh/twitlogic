package edu.rpi.tw.twctwit.query;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.vocabs.DCTerms;
import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.vocabs.RDF;
import net.fortytwo.twitlogic.vocabs.SIOC;
import net.fortytwo.twitlogic.vocabs.SIOCT;
import org.openrdf.sail.SailConnection;
import net.fortytwo.sesametools.ldserver.query.SparqlTools;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class QueryPlay {
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
                    try {
                        sc.begin();
                        SparqlTools.executeQuery(GOLD_TWEETS_QUERY, sc, System.out, 100, SparqlTools.SparqlResultFormat.JSON);
                    } finally {
                        sc.rollback();
                        sc.close();
                    }
                    //queryAndWriteJSON(ISWC_STATEMENTS_QUERY, sc, System.out);
                } finally {
                    c.close();
                }
            } finally {
                store.shutDown();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
