package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Apr 1, 2010
 * Time: 8:11:35 PM
 */
public class PeriodicDumpfileGenerator implements Runnable {
    private static final Logger LOGGER = TwitLogic.getLogger(PeriodicDumpfileGenerator.class);

    private final TypedProperties conf;
    private final long dumpInterval;
    private TweetStore tweetStore;

    public PeriodicDumpfileGenerator(final TweetStore tweetStore,
                                     final TypedProperties conf) throws PropertyException {
        this.tweetStore = tweetStore;
        this.conf = conf;
        dumpInterval = conf.getLong(TwitLogic.DUMPINTERVAL);
    }

    public void run() {
        while (true) {
            try {
                // Note: this uncompressed file is generated only for the
                // sake of the Linking Open Conference Tweets application
                // (in case we put it back up)
                File f1 = new File(conf.getFile(TwitLogic.SERVER_STATICCONTENTDIRECTORY),
                        "dump/twitlogic-full.rdf");
                tweetStore.dumpToFile(f1, RDFFormat.RDFXML);

                // TODO: use N-Quads instead of (or in addition to) TriG
                File f2 = new File(conf.getFile(TwitLogic.SERVER_STATICCONTENTDIRECTORY),
                        "dump/twitlogic-full.trig.gz");
                tweetStore.dumpToCompressedFile(f2, RDFFormat.TRIG);
            } catch (Throwable t) {
                LOGGER.severe("dumper runnable died with error: " + t);
                t.printStackTrace();
                return;
            }

            try {
                Thread.sleep(dumpInterval);
            } catch (InterruptedException e) {
                LOGGER.severe("dumper runnable died with error: " + e);
                e.printStackTrace();
                return;
            }
        }
    }
}
