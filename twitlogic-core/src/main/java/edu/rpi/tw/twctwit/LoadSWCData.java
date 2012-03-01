package edu.rpi.tw.twctwit;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreException;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class LoadSWCData {
    private static final String SWC_DIR = "edu.rpi.tw.twctwit.swcDir";
    private static final URI SWC_GRAPH = new URIImpl("http://twitlogic.fortytwo.net/graph/swc");

    public static void main(final String[] args) throws Exception {
        try {
            if (1 == args.length) {
                File configFile = new File(args[0]);
                Properties p = new Properties();
                p.load(new FileInputStream(configFile));
                TwitLogic.setConfiguration(p);

                load();
            } else {
                printUsage();
                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:  LoadSWCData [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://wiki.github.com/joshsh/twitlogic/configuring-and-running-twitlogic>.");
    }


    private static final void load() throws TweetStoreException, PropertyException, RepositoryException, IOException, RDFParseException {
        System.out.println("Loading Semantic Web Conference Corpus data");
        // Create a persistent store.
        TweetStore store = new TweetStore();
        store.initialize();
        File dir = TwitLogic.getConfiguration().getFile(SWC_DIR);
        try {
            RepositoryConnection rc = store.getRepository().getConnection();
            try {
                rc.clear(SWC_GRAPH);
                rc.commit();
                loadFile(dir, rc);
            } finally {
                rc.close();
            }
        } finally {
            store.shutDown();
        }
        System.out.println("done.");
    }

    private static final String BASE_URI = "http://example.org/bogoBaseURI#";

    private static void loadFile(final File file,
                                 final RepositoryConnection rc) throws RepositoryException, IOException, RDFParseException {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                loadFile(f, rc);
            }
        } else if (file.getName().endsWith("rdf")) {
            rc.add(file, BASE_URI, RDFFormat.RDFXML, SWC_GRAPH);
            rc.commit();
        }
    }
}
