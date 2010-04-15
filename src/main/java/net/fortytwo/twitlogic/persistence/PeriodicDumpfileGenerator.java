package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Apr 1, 2010
 * Time: 8:11:35 PM
 */
// TODO: add support for N-Quads
public class PeriodicDumpfileGenerator implements Runnable {
    private static final Logger LOGGER = TwitLogic.getLogger(PeriodicDumpfileGenerator.class);

    private TweetStore tweetStore;
    private final long interval;
    private final File file;
    private final RDFFormat format;
    private final boolean compressed;

    public PeriodicDumpfileGenerator(final TweetStore tweetStore,
                                     final File file,
                                     final RDFFormat format,
                                     final boolean compressed,
                                     final long interval) throws PropertyException {
        this.tweetStore = tweetStore;
        this.interval = interval;
        this.file = file;
        this.compressed = compressed;
        this.format = format;
    }

    public void run() {
        while (true) {
            try {
                if (compressed) {
                    tweetStore.dumpToCompressedFile(file, format);
                } else {
                    tweetStore.dumpToFile(file, format);
                }
            } catch (Throwable t) {
                LOGGER.severe("dumper runnable died with error: " + t);
                t.printStackTrace();
                return;
            }

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                LOGGER.severe("dumper runnable died with error: " + e);
                e.printStackTrace();
                return;
            }
        }
    }
}
