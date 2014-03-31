package net.fortytwo.twitlogic.util;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.Place;
import net.fortytwo.twitlogic.persistence.PersistenceContext;
import net.fortytwo.twitlogic.persistence.PlacePersistenceHelper;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.TweetStoreConnection;
import net.fortytwo.twitlogic.persistence.beans.Feature;
import net.fortytwo.twitlogic.services.twitter.CustomTwitterClient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class PlaceMiner {
    private static final Logger LOGGER = TwitLogic.getLogger(PlaceMiner.class);

    public static void main(final String[] args) {
        try {
            if (2 == args.length) {
                File configFile = new File(args[0]);
                Properties p = new Properties();
                p.load(new FileInputStream(configFile));
                TwitLogic.setConfiguration(p);

                File idsFile = new File(args[1]);

                mine(getIds(idsFile));
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
        System.out.println("Usage:  placeminer [configuration file] [ids file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://wiki.github.com/joshsh/twitlogic/configuring-and-running-twitlogic>.");
    }

    private static void mine(final Collection<String> ids) throws Exception {
        CustomTwitterClient client = new CustomTwitterClient();

        TweetStore store = new TweetStore();
        store.initialize();

        try {
            TweetStoreConnection c = store.createConnection();

            try {
                PersistenceContext pc
                        = new PersistenceContext(c.getElmoManager());

                PlacePersistenceHelper ph = new PlacePersistenceHelper(pc, client, false);
                for (String id : ids) {
                    Place p = new Place(id);
                    Feature f = pc.persist(p);
                    ph.submit(p, f);
                    c.commit();
                }
            } finally {
                c.rollback();
                c.close();
            }
        } finally {
            store.shutDown();
        }
    }

    private static Collection<String> getIds(final File idFile) throws IOException {
        Collection<String> ids = new LinkedList<String>();

        FileInputStream fstream = new FileInputStream(idFile);
        DataInputStream in = new DataInputStream(fstream);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String id;
            while ((id = br.readLine()) != null) {
                ids.add(id);
            }
        } finally {
            in.close();
        }

        return ids;
    }
}
