package edu.rpi.tw.twctwit.query;

import edu.rpi.tw.patadata.RDFSpreadVector;
import edu.rpi.tw.patadata.ranking.WeightedValue;
import edu.rpi.tw.patadata.ranking.WeightedVector;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.vocabs.OWL;
import net.fortytwo.twitlogic.vocabs.SIOC;
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
public class RelatedHashtagsInferencer extends RDFSpreadVector {
    private static final URI[] IN_PREDICATES = new URI[]{
            new URIImpl(OWL.SAMEAS),
            new URIImpl(SIOC.TOPIC),
    };

    private static final URI[] OUT_PREDICATES = new URI[]{
            new URIImpl(OWL.SAMEAS),
            new URIImpl(SIOC.TOPIC),
    };

    public RelatedHashtagsInferencer(final SailConnection sailConnection,
                                     final Resource... seeds) {
        super(new WeightedVector<Resource>(), IN_PREDICATES, OUT_PREDICATES, sailConnection, seeds);
    }

    public Collection<Resource> currentHashtagResults(final int limit) {
        Collection<Resource> tags = new LinkedList<Resource>();

        int i = 0;
        for (WeightedValue<Resource> wv : currentResult().toSortedArray()) {
            Resource r = wv.value;
            if (r.toString().startsWith(TwitLogic.HASHTAGS_BASEURI)) {
                if (++i > limit) {
                    break;
                } else {
                    tags.add(r);
                }
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
                    URI iswc2009 = new URIImpl("http://twitlogic.fortytwo.net/hashtag/linkeddata");
                    RelatedHashtagsInferencer inf = new RelatedHashtagsInferencer(c.getSailConnection(), iswc2009);

                    int steps = 500;
                    int used = inf.compute(steps);

                    for (WeightedValue<Resource> wv : inf.currentResult().toSortedArray()) {
                        if (wv.value.toString().contains("hashtag")) {
//                            System.out.println("" + wv.weight + "\t" + wv.value);
                            System.out.println(((URI) wv.value).getLocalName());
                        }
                    }


//                    Collection<Resource> results = inf.currentResult();
                    Collection<Resource> results = inf.currentHashtagResults(20);

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