package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Apr 1, 2010
 * Time: 8:11:35 PM
 */
// TODO: add support for N-Quads
public class DumpFileGeneratorTask extends TimerTask {
    private static final Logger LOGGER = TwitLogic.getLogger(DumpFileGeneratorTask.class);

    private TweetStore tweetStore;
    private final File file;
    private final RDFFormat format;
    private final boolean compressed;

    public DumpFileGeneratorTask(final TweetStore tweetStore,
                                 final File file,
                                 final RDFFormat format,
                                 final boolean compressed) {
        this.tweetStore = tweetStore;
        this.file = file;
        this.compressed = compressed;
        this.format = format;

        file.getParentFile().mkdirs();
    }

    public void run() {
        try {
            if (compressed) {
                tweetStore.dumpToCompressedFile(file, format);
            } else {
                tweetStore.dumpToFile(file, format);
            }
        } catch (Throwable t) {
            LOGGER.severe("failed to generate dump file: " + t);
            t.printStackTrace();
        }
    }
}
