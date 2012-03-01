package edu.rpi.tw.twctwit;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.TweetStore;

import java.io.FileInputStream;
import java.io.File;
import java.util.Properties;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TWCTwitDemo {

    public static void main(final String[] args) throws Exception {
        try {
            if (1 == args.length) {
                File configFile = new File(args[0]);
                Properties p = new Properties();
                p.load(new FileInputStream(configFile));
                TwitLogic.setConfiguration(p);

                runDemo();
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
        System.out.println("Usage:  twctwit [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://wiki.github.com/joshsh/twitlogic/configuring-and-running-twitlogic>.");
    }

    private static void runDemo() throws Exception {
        // Create a persistent store.
        TweetStore store = new TweetStore();
        store.initialize();

        try {
            //store.dump(System.out);
            //store.dumpToFile(new File("/tmp/twitlogic-tmp-dump.trig"), RDFFormat.TRIG);

            // Launch linked data server.
            store.startServer();

            Object mutex = "";
            synchronized (mutex) {
                mutex.wait();
            }
        } finally {
            store.shutDown();
        }
    }
}