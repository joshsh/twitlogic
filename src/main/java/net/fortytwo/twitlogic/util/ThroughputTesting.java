package net.fortytwo.twitlogic.util;

import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import net.fortytwo.sesametools.rdftransaction.RDFTransactionSail;
import net.fortytwo.sesametools.replay.RecorderSail;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.flow.NullHandler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.TweetParseException;
import net.fortytwo.twitlogic.persistence.SailFactory;
import net.fortytwo.twitlogic.persistence.TweetPersister;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.persistence.sail.NewAllegroSailFactory;
import net.fortytwo.twitlogic.services.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.services.twitter.TwitterAPI;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * User: josh
 * Date: Aug 5, 2010
 * Time: 3:10:06 PM
 */
public class ThroughputTesting {
    private final int udpOutputPorts[] = {9990, 9991, 9992, 9993, 9994, 9995};//, 9996, 9997};

    private static final Random RANDOM = new Random();
    private static final Date REFERENCE_DATE = new Date();

    public static void main(final String[] args) throws Exception {
        Properties config = new Properties();
        config.load(new FileInputStream("/tmp/twitlogic-throughput-testing.properties"));
        TwitLogic.setConfiguration(config);
        ThroughputTesting t = new ThroughputTesting();

        //rdfTransactionStuff();

        //testNullHandler();
        //testMemoryPersister();
        //testTransientMemoryPersister();
        //testLoggingTransientMemoryPersister();
        //testNativeStorePersister();
        //t.testSocketBasedLoggingTransientMemoryPersister();
        //t.testRdfTransactionPersister(1);
        //t.testRdfTransactionPersister(100);
        //t.testTrivialRdfTransactionPersister();
        //t.testAllegroGraphPersister();
        t.testUdpTransactionPersister();

/*
        //System.out.println("" + Integer.MAX_VALUE);
        for (int i = 0; i < 10; i++) {
//            System.out.println(randomDateString());
//            System.out.println(randomString(70, 140));
//            System.out.println(randomUrlString(10, 50));
//            System.out.println(randomUrl());
//            System.out.println(randomTweet());
        }
        //*/
    }

    private static void rdfTransactionStuff() throws Exception {
        Sail baseSail = new MemoryStore();
        baseSail.initialize();

        RDFTransactionSail sail = new RDFTransactionSail(baseSail) {
            public void uploadTransactionEntity(byte[] bytes) throws SailException {
                System.out.println(new String(bytes));
            }
        };

        SailConnection sc = sail.getConnection();
        try {
            sc.removeStatements(RDF.TYPE, null, null, (URI) null);
            sc.commit();
        } finally {
            sc.close();
        }

        sail.shutDown();
        baseSail.shutDown();
    }

    ////////////////////////////////////////////////////////////////////////////

    // Around 14,000 t/s on the reference machine.

    private void testNullHandler() throws Exception {
        Handler<Tweet, TweetHandlerException> h = new NullHandler<Tweet, TweetHandlerException>();
        stressTest(h, 10000);
    }

    // Quickly reaches a peak of around 1,900 t/s before slowing down and failing due to lack of memory.
    private void testMemoryPersister() throws Exception {
        Sail sail = new MemoryStore();
        sail.initialize();

        try {
            TweetStore store = new TweetStore(sail);
            store.initialize();
            try {
                TweetPersister p = new TweetPersister(store, null);

                stressTest(p, 1000);
            } finally {
                store.shutDown();
            }
        } finally {
            sail.shutDown();
        }
    }

    // Reaches a peak of around 2,100 t/s and remains there indefinitely
    private void testTransientMemoryPersister() throws Exception {
        Sail sail = new MemoryStore();
        sail.initialize();

        try {
            TweetStore store = new TweetStore(sail);
            store.initialize();
            try {
                final SailConnection sc = sail.getConnection();
                try {
                    final TweetPersister p = new TweetPersister(store, null);

                    Handler<Tweet, TweetHandlerException> h = new Handler<Tweet, TweetHandlerException>() {
                        public boolean handle(final Tweet tweet) throws TweetHandlerException {
                            try {
                                sc.clear();
                                sc.commit();
                            } catch (SailException e) {
                                throw new TweetHandlerException(e);
                            }
                            return p.handle(tweet);
                        }
                    };

                    stressTest(h, 1000);
                } finally {
                    sc.close();
                }
            } finally {
                store.shutDown();
            }
        } finally {
            sail.shutDown();
        }
    }

    // Around 900 t/s (up from 500 t/s before omitting read-operation logging)
    private void testLoggingTransientMemoryPersister() throws Exception {
        Sail baseSail = new MemoryStore();
        baseSail.initialize();

        OutputStream out = new FileOutputStream(new File("/tmp/throughput-test.log"));
        RecorderSail sail = new RecorderSail(baseSail, out);
        sail.getConfiguration().logReadOperations = false;
        //sail.getConfiguration().logTransactions = false;

        try {
            TweetStore store = new TweetStore(sail);
            store.initialize();
            try {
                final SailConnection sc = baseSail.getConnection();
                try {
                    final TweetPersister p = new TweetPersister(store, null);

                    Handler<Tweet, TweetHandlerException> h = new Handler<Tweet, TweetHandlerException>() {
                        public boolean handle(final Tweet tweet) throws TweetHandlerException {
                            try {
                                sc.clear();
                                sc.commit();
                            } catch (SailException e) {
                                throw new TweetHandlerException(e);
                            }
                            return p.handle(tweet);
                        }
                    };

                    stressTest(h, 1000);
                } finally {
                    sc.close();
                }
            } finally {
                store.shutDown();
            }
        } finally {
            sail.shutDown();
        }
    }

    // Over the LAN:
    //     1 trans/upload -- 60 t/s
    //     100 trans/upload -- 160 t/s
    private void testRdfTransactionPersister(final int commitsPerUpload) throws Exception {
        AGRepository repo = new NewAllegroSailFactory(TwitLogic.getConfiguration(), false).makeAGRepository();
        repo.initialize();
        try {
            AGRepositoryConnection rc = repo.getConnection();
            try {
                Sail tSail = new MemoryStore();
                tSail.initialize();

                try {
                    Sail sail = new AGTransactionSail(tSail, rc, commitsPerUpload);
                    try {

                        TweetStore store = new TweetStore(sail);
                        store.doNotRefreshCoreMetadata();
                        store.initialize();
                        try {
                            final SailConnection tc = tSail.getConnection();
                            try {
                                final TweetPersister p = new TweetPersister(store, null);

                                Handler<Tweet, TweetHandlerException> h = new Handler<Tweet, TweetHandlerException>() {
                                    public boolean handle(final Tweet tweet) throws TweetHandlerException {
                                        try {
                                            tc.clear();
                                            tc.commit();
                                        } catch (SailException e) {
                                            throw new TweetHandlerException(e);
                                        }

                                        return p.handle(tweet);
                                    }
                                };

                                stressTest(h, 100);
                            } finally {
                                tc.close();
                            }
                        } finally {
                            store.shutDown();
                        }
                    } finally {
                        sail.shutDown();
                    }
                } finally {
                    tSail.shutDown();
                }


            } finally {
                rc.close();
            }
        } finally {
            repo.shutDown();
        }
    }

    // Around 1000 t/s on the reference machine.  No network requests are made.
    private void testTrivialRdfTransactionPersister() throws Exception {
        Sail transientSail = new MemoryStore();
        transientSail.initialize();

        try {
            Sail sail = new TrivialTransactionSail(transientSail);
            try {
                TweetStore store = new TweetStore(sail);
                store.initialize();

                try {
                    final SailConnection tc = transientSail.getConnection();

                    try {
                        final TweetPersister p = new TweetPersister(store, null);

                        Handler<Tweet, TweetHandlerException> h = new Handler<Tweet, TweetHandlerException>() {
                            public boolean handle(final Tweet tweet) throws TweetHandlerException {
                                try {
                                    tc.clear();
                                    tc.commit();
                                } catch (SailException e) {
                                    throw new TweetHandlerException(e);
                                }
                                return p.handle(tweet);
                            }
                        };

                        stressTest(h, 1000);
                    } finally {
                        tc.close();
                    }
                } finally {
                    store.shutDown();
                }
            } finally {
                sail.shutDown();
            }
        } finally {
            transientSail.shutDown();
        }
    }

    private void testSocketBasedLoggingTransientMemoryPersister() throws Exception {
        Sail baseSail = new MemoryStore();
        baseSail.initialize();

        DatagramSocket s = new DatagramSocket();

        String msg = "15663\tADD_STATEMENT\t<http://twitlogic.fortytwo.net/post/twitter/-1129589402>\t<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>\t<http://rdfs.org/sioc/types#MicroblogPost>\t{}";
        byte[] buf = msg.getBytes();
        InetAddress address = InetAddress.getByName("foray");
        for (int i = 0; i < 50; i++)
            s.send(new DatagramPacket(buf, buf.length, address, 9999));

        /*
   OutputStream out = new FileOutputStream(new File("/tmp/throughput-test.log"));
   RecorderSail sail = new RecorderSail(baseSail, out);
   sail.getConfiguration().logReadOperations = false;
   //sail.getConfiguration().logTransactions = false;

   try {
       TweetStore store = new TweetStore(sail);
       store.initialize();
       try {
           final SailConnection sc = baseSail.getConnection();
           try {
               final TweetPersister p = new TweetPersister(store, null);

               Handler<Tweet, TweetHandlerException> h = new Handler<Tweet, TweetHandlerException>() {
                   public boolean handle(final Tweet tweet) throws TweetHandlerException {
                       try {
                           sc.clear();
                           sc.commit();
                       } catch (SailException e) {
                           throw new TweetHandlerException(e);
                       }
                       return p.handle(tweet);
                   }
               };

               stressTest(h, 1000);
           } finally {
               sc.close();
           }
       } finally {
           store.shutDown();
       }
   } finally {
       sail.shutDown();
   }     */
    }

    // Around 300 t/s for a small store.
    private void testNativeStorePersister() throws Exception {
        File dir = new File("/tmp/twitlogic-stresstest-ns");
        if (dir.exists()) {
            deleteDirectory(dir);
        }

        Sail sail = new NativeStore(dir);
        sail.initialize();

        try {
            TweetStore store = new TweetStore(sail);
            store.initialize();
            try {
                TweetPersister p = new TweetPersister(store, null);

                stressTest(p, 1000);
            } finally {
                store.shutDown();
            }
        } finally {
            sail.shutDown();
        }
    }

    // Over the LAN: 6 t/s
    // Locally: 7 t/s
    private void testAllegroGraphPersister() throws Exception {
        SailFactory f = new NewAllegroSailFactory(TwitLogic.getConfiguration(), false);
        Sail sail = f.makeSail();
        sail.initialize();

        try {
            TweetStore store = new TweetStore(sail);
            store.initialize();
            try {
                TweetPersister p = new TweetPersister(store, null);

                stressTest(p, 10);
            } finally {
                store.shutDown();
            }
        } finally {
            sail.shutDown();
        }
    }

    // Around 1000 t/s on (my MacBook Pro)-->(AG foray)
    private void testUdpTransactionPersister() throws Exception {
        InetAddress address = InetAddress.getByName("fluxdmz");
//            InetAddress address = InetAddress.getByName("foray");
        //int port = 9999;

        Sail workingSail = new MemoryStore();
        workingSail.initialize();

        try {
            Sail streamingSail = new UdpTransactionSail(workingSail, address, udpOutputPorts);

            try {
                TweetStore store = new TweetStore(streamingSail);
                store.doNotRefreshCoreMetadata();
                store.initialize();

                try {
                    // A connection with which to repeatedly clear the working store
                    final SailConnection c = workingSail.getConnection();

                    try {
                        // Offline persister
                        final TweetPersister p = new TweetPersister(store, null);

                        try {
                            Handler<Tweet, TweetHandlerException> h = new Handler<Tweet, TweetHandlerException>() {
                                public boolean handle(final Tweet tweet) throws TweetHandlerException {
                                    try {
                                        c.clear();
                                        c.commit();
                                    } catch (SailException e) {
                                        throw new TweetHandlerException(e);
                                    }

                                    return p.handle(tweet);
                                }
                            };

                            stressTest(h, 1000);
                        } finally {
                            p.close();
                        }
                    } finally {
                        c.close();
                    }
                } finally {
                    store.shutDown();
                }
            } finally {
                streamingSail.shutDown();
            }
        } finally {
            workingSail.shutDown();
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private class AGTransactionSail extends RDFTransactionSail {
        private final AGRepositoryConnection connection;

        public AGTransactionSail(final Sail sail,
                                 final AGRepositoryConnection connection,
                                 final int commitsPerUpload) {
            super(sail, commitsPerUpload);
            this.connection = connection;
        }

        public void uploadTransactionEntity(byte[] bytes) throws SailException {
            RequestEntity entity = new ByteArrayRequestEntity(bytes, "application/x-rdftransaction");
            try {
                //System.out.println("uploading!");
                connection.getHttpRepoClient().upload(entity, null, false, null, null, null);
            } catch (IOException e) {
                throw new SailException(e);
            } catch (RDFParseException e) {
                throw new SailException(e);
            } catch (RepositoryException e) {
                throw new SailException(e);
            }
        }
    }

    private class TrivialTransactionSail extends RDFTransactionSail {
        public TrivialTransactionSail(final Sail sail) {
            super(sail);
        }

        public void uploadTransactionEntity(byte[] bytes) throws SailException {
            // Generate the entity, but do nothing with it.
            RequestEntity entity = new ByteArrayRequestEntity(bytes, "application/x-rdftransaction");
        }
    }

    private class MultithreadedTrivialTransactionSail extends RDFTransactionSail {
        public MultithreadedTrivialTransactionSail(final Sail sail,
                                                   final int threads) {
            super(sail);
        }

        public void uploadTransactionEntity(byte[] bytes) throws SailException {
            // Generate the entity, but do nothing with it.
            RequestEntity entity = new ByteArrayRequestEntity(bytes, "application/x-rdftransaction");
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private static boolean deleteDirectory(final File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return (dir.delete());
    }

    private static int randomInteger(final int min,
                                     final int max) {
        return min + RANDOM.nextInt(1 + max - min);
    }

    private static String randomString(final int minLength,
                                       final int maxLength) {
        int l = randomInteger(minLength, maxLength);
        byte[] b = new byte[l];
        for (int i = 0; i < l; i++) {
            b[i] = (byte) (35 + RANDOM.nextInt(56));
        }
        return new String(b);
    }

    private static String randomUrlString(final int minLength,
                                          final int maxLength) {
        int l = randomInteger(minLength, maxLength);
        byte[] b = new byte[l];
        for (int i = 0; i < l; i++) {
            b[i] = (byte) (47 + RANDOM.nextInt(11));
        }
        return new String(b);
    }

    private static String randomUrl() {
        return "http://example.org/" + randomUrlString(10, 100);
    }

    private static String randomDateString() {
        Date d = new Date(REFERENCE_DATE.getTime() - RANDOM.nextInt());
        return TwitterAPI.DATE_FORMAT.format(d);
    }

    // Currently, each tweet contributes around 20 triples.
    private static Tweet randomTweet() throws JSONException, TweetParseException {
        String text = randomString(30, 140);
        String createdAt = randomDateString();
        String profileImageUrl = randomUrl();
        String description = randomString(30, 140);
        String location = randomString(15, 50);
        String screenName = randomString(5, 20);
        String url = randomUrl();
        String name = randomString(10, 30);
        int userId = randomInteger(0, Integer.MAX_VALUE - 1);
        long tweetId = RANDOM.nextInt();

        StringBuilder sb = new StringBuilder();
        sb.append("{\n" +
                "    \"coordinates\":null,\n" +
                "    \"in_reply_to_user_id\":null,\n" +
                "    \"text\":\"").append(text).append("\",\n" +
                "    \"contributors\":null,\n" +
                "    \"favorited\":false,\n" +
                "    \"created_at\":\"").append(createdAt).append("\",\n" +
                "    \"source\":\"<a href=\\\"http://twitterrific.com\\\" rel=\\\"nofollow\\\">Twitterrific</a>\",\n" +
                "    \"geo\":null,\n" +
                "    \"in_reply_to_status_id\":null,\n" +
                "    \"truncated\":false,\n" +
                "    \"place\":null,\n" +
                "    \"in_reply_to_screen_name\":null,\n" +
                "    \"user\":\n" +
                "    {\n" +
                "        \"profile_background_image_url\":\"http://a1.twimg.com/profile_background_images/58551286/fish_twitter_background.jpg\",\n" +
                "        \"contributors_enabled\":false,\n" +
                "        \"profile_background_color\":\"1A1B1F\",\n" +
                "        \"profile_background_tile\":true,\n" +
                "        \"created_at\":\"Tue Jun 26 05:26:08 +0000 2007\",\n" +
                "        \"profile_image_url\":\"").append(profileImageUrl).append("\",\n" +
                "        \"profile_text_color\":\"666666\",\n" +
                "        \"followers_count\":110,\n" +
                "        \"description\":\"").append(description).append("\",\n" +
                "        \"lang\":\"en\",\n" +
                "        \"verified\":false,\n" +
                "        \"location\":\"").append(location).append("\",\n" +
                "        \"screen_name\":\"").append(screenName).append("\",\n" +
                "        \"following\":null,\n" +
                "        \"friends_count\":93,\n" +
                "        \"profile_link_color\":\"2FC2EF\",\n" +
                "        \"notifications\":null,\n" +
                "        \"favourites_count\":18,\n" +
                "        \"profile_sidebar_fill_color\":\"252429\",\n" +
                "        \"protected\":false,\n" +
                "        \"url\":\"").append(url).append("\",\n" +
                "        \"name\":\"").append(name).append("\",\n" +
                "        \"geo_enabled\":true,\n" +
                "        \"time_zone\":\"Eastern Time (US & Canada)\",\n" +
                "        \"profile_sidebar_border_color\":\"181A1E\",\n" +
                "        \"id\":").append(userId).append(",\n" +
                "        \"statuses_count\":550,\n" +
                "        \"utc_offset\":-18000\n" +
                "    },\n" +
                "    \"id\":").append(tweetId).append("\n" +
                "}");
        return new Tweet(new JSONObject(sb.toString()));
    }

    private static void stressTest(final Handler<Tweet, TweetHandlerException> handler,
                                   final long chunkSize) throws JSONException, TweetParseException, TweetHandlerException {
        while (true) {
            long before = new Date().getTime();
            for (long i = 0; i < chunkSize; i++) {
                Tweet t = randomTweet();
                if (!handler.handle(t)) {
                    return;
                }
            }
            long duration = new Date().getTime() - before;
            System.out.println("" + chunkSize + " tweets in " + duration + "ms (" + (chunkSize * 1000 / duration) + " tweets/s)");
        }
    }
}
