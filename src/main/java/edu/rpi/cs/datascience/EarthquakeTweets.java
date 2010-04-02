package edu.rpi.cs.datascience;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.TwitLogicAgent;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.persistence.TweetStoreException;
import net.fortytwo.twitlogic.persistence.UserRegistry;
import net.fortytwo.twitlogic.twitter.CommandListener;
import net.fortytwo.twitlogic.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.twitter.TwitterClient;
import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 * User: josh
 * Date: Nov 20, 2009
 * Time: 4:17:01 PM
 */
public class EarthquakeTweets {

    private static final String
            TIMESTAMP = "timestamp",
            LOCATION = "location",
            TEXT = "text";

    private static final String
            SELECT_DUMP_FIELDS = "" +
            "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
            "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "SELECT ?" + TIMESTAMP + " ?" + LOCATION + " ?" + TEXT + "\n" +
            "WHERE { ?item sioc:has_creator ?user .\n" +
            "    ?user sioc:account_of ?person.\n" +
            "    ?item dcterms:created ?" + TIMESTAMP + ".\n" +
            "    ?person foaf:based_near ?place.\n" +
            "    ?place rdfs:comment ?" + LOCATION + ".\n" +
            "    ?item sioc:content ?" + TEXT + ".\n" +
            "}";

    public static void main(final String[] args) throws Exception {
        System.out.println(SELECT_DUMP_FIELDS);
        System.exit(0);
        try {
            doit();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void doit() throws Exception {
        try {
            Properties conf = new Properties();
            conf.load(EarthquakeTweets.class.getResourceAsStream("datascience.properties"));
            TwitLogic.setConfiguration(conf);

            // Create a persistent store.
            TweetStore store = new TweetStore();
            store.initialize();

            try {
                //store.dump(System.out);
                dumpTabSeparatedFile(store, new File("/tmp/earthquaketweets.txt"));

                TwitterClient client = new TwitterClient();
                UserRegistry userRegistry = new UserRegistry(client);
                TweetStoreConnection c = store.createConnection();
                try {
                    boolean persistUnannotatedTweets = true;
                    TweetPersister baseStatusHandler = new TweetPersister(store, c, client, persistUnannotatedTweets);

                    // Create an agent to listen for commands.
                    // Also take the opportunity to memoize users we're following.
                    TwitLogicAgent agent = new TwitLogicAgent(client);
                    Handler<Tweet, TweetHandlerException> statusHandler
                            = userRegistry.createUserRegistryFilter(
                            new CommandListener(agent, baseStatusHandler));

                    String[] keywords = {"earthquake"};
                    client.processTrackFilterStream(keywords, statusHandler);

                    System.out.println("Done.");
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

    private static void dumpTabSeparatedFile(final TweetStore store,
                                             final File outputFile) throws TweetStoreException, MalformedQueryException, SailException, QueryEvaluationException, IOException {
        OutputStream out = new FileOutputStream(outputFile);
        PrintStream ps = new PrintStream(out);

        try {
            TweetStoreConnection c = store.createConnection();
            try {
                ParsedQuery q = parseQuery(SELECT_DUMP_FIELDS);
                BindingSet bs = new MapBindingSet();
                SailConnection sc = c.getSailConnection();
                CloseableIteration<? extends BindingSet, QueryEvaluationException> results
                        = sc.evaluate(q.getTupleExpr(), q.getDataset(), bs, false);
                try {
                    while (results.hasNext()) {
                        BindingSet r = results.next();
                        String timestamp = ((Literal) r.getBinding(TIMESTAMP).getValue()).getLabel().replaceAll("\t", " ");
                        String location = ((Literal) r.getBinding(LOCATION).getValue()).getLabel().replaceAll("\t", " ");
                        String text = ((Literal) r.getBinding(TEXT).getValue()).getLabel()
                                .replaceAll("\t", " ")
                                .replaceAll("\n", " ")
                                .replaceAll("\r", " ");

                        ps.println(timestamp + "\t" + location + "\t" + text);
                    }
                } finally {
                    results.close();
                }
            } finally {
                c.close();
            }
        } finally {
            out.close();
        }
    }

    private static ParsedQuery parseQuery(final String query) throws MalformedQueryException {
        SPARQLParser parser = new SPARQLParser();
        String baseURI = "http://example.org/bogusBaseURI/";
        return parser.parseQuery(query, baseURI);
    }
}
