package edu.rpi.tw.twctwit.query;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.vocabs.OWL;
import net.fortytwo.twitlogic.vocabs.PML2Relation;
import net.fortytwo.twitlogic.vocabs.SWC;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.SailConnection;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * User: josh
 * Date: Apr 25, 2010
 * Time: 8:05:45 PM
 */
public class ConferenceInferencer extends SimpleInferencer {
    private static final URI[] IN_PREDICATES = new URI[]{
            new URIImpl(OWL.SAMEAS),
            new URIImpl(SWC.IS_SUBEVENT_OF),
            new URIImpl(PML2Relation.ISPARTOF),
    };

    private static final URI[] OUT_PREDICATES = new URI[]{
            new URIImpl(OWL.SAMEAS),
            new URIImpl(SWC.IS_SUPER_EVENT_OF),
    };

    public ConferenceInferencer(final SailConnection sailConnection,
                                final Resource... seeds) {
        super(IN_PREDICATES, OUT_PREDICATES, sailConnection, seeds);
    }

    public Collection<Resource> currentHashtagResults() {
        Collection<Resource> tags = new LinkedList<Resource>();

        for (Resource r : currentResult()) {
            // Note: the actual base URI depends on the host name of the TwitLogic server.
            // Simply filtering on "hashtag" will remove most non-hashtag resources.
      //      if (r.toString().contains("hashtag")) {
            if (r.toString().startsWith(TwitLogic.HASHTAGS_BASEURI)) {
                tags.add(r);
            }
        }

        return tags;
    }

    public static void main(final String[] args) throws Exception {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("/Users/josh/projects/fortytwo/twitlogic/config/twitlogic.properties"));
            TwitLogic.setConfiguration(props);

            // Create a persistent store.
            TweetStore store = new TweetStore();
            store.initialize();

            try {
                TweetStoreConnection c = store.createConnection();
                try {
                    URI iswc2009 = new URIImpl("http://data.semanticweb.org/conference/iswc/2009");
                    ConferenceInferencer inf = new ConferenceInferencer(c.getSailConnection(), iswc2009);

                    int steps = 50;
                    int used = inf.compute(steps);
//                    Collection<Resource> results = inf.currentResult();
                    Collection<Resource> results = inf.currentHashtagResults();

                    System.out.println("" + used + " of " + steps + " cycles used.  Results:");
                    for (Resource r : results) {
                        System.out.println("\t" + r);
                    }
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
