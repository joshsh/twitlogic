package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.beans.Agent;
import net.fortytwo.twitlogic.persistence.beans.Document;
import net.fortytwo.twitlogic.persistence.beans.Graph;
import net.fortytwo.twitlogic.persistence.beans.Image;
import net.fortytwo.twitlogic.persistence.beans.MicroblogPost;
import net.fortytwo.twitlogic.persistence.beans.SpatialThing;
import net.fortytwo.twitlogic.persistence.beans.User;
import net.fortytwo.twitlogic.util.SparqlUpdateTools;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.concepts.owl.ObjectProperty;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.ElmoManagerFactory;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * User: josh
 * Date: Oct 2, 2009
 * Time: 9:10:17 PM
 */
public class TweetStore {
    private static final Logger LOGGER = TwitLogic.getLogger(TwitLogic.class);

    private static TweetStore defaultStore;

    private final Sail sail;
    private Repository repository;
    private ElmoModule adminElmoModule;
    private SesameManagerFactory elmoManagerFactory;
    private boolean initialized = false;

    public static TweetStore getDefaultStore() throws TweetStoreException {
        if (null == defaultStore) {
            defaultStore = new TweetStore();
            defaultStore.initialize();

            Runtime.getRuntime().addShutdownHook(new Thread("shutdown hook for default TwitLogic store") {
                @Override
                public void run() {
                    try {
                        defaultStore.shutDown();
                        //defaultStore.getSail().shutDown();
                    } catch (Throwable t) {
                        LOGGER.severe("failure in store shutdown: " + t.getMessage());
                    }
                }
            });
        }

        return defaultStore;
    }

    private TweetStore() throws TweetStoreException {
        this(TwitLogic.getConfiguration());
    }

    public TweetStore(final TypedProperties props) throws TweetStoreException {
        String sailType;
        try {
            sailType = props.getString(TwitLogic.SAIL_CLASS);
        } catch (PropertyException e) {
            throw new TweetStoreException(e);
        }
        sail = createSail(sailType);
    }

    public TweetStore(final Sail sail) {
        this.sail = sail;
    }

    public void initialize() throws TweetStoreException {
        if (initialized) {
            throw new IllegalStateException("store has already been initialized");
        }

        LOGGER.info("initializing TwitLogic local store");

        repository = new SailRepository(sail);
        addSeedDataIfEmpty(repository);

        // Elmo setup.
        adminElmoModule = new ElmoModule();
        adminElmoModule.setGraph(new QName(SesameTools.ADMIN_GRAPH.toString()));
        adminElmoModule.addConcept(Thing.class);
        adminElmoModule.addConcept(ObjectProperty.class);  // Dunno why this is necessary, but Elmo logs warnings without it

        // TwitLogic-specific classes
        adminElmoModule.addConcept(Agent.class);
        adminElmoModule.addConcept(Document.class);
        adminElmoModule.addConcept(Graph.class);
        adminElmoModule.addConcept(Image.class);
        adminElmoModule.addConcept(MicroblogPost.class);
        adminElmoModule.addConcept(SpatialThing.class);
        adminElmoModule.addConcept(User.class);

        elmoManagerFactory
                = new SesameManagerFactory(adminElmoModule, repository);
        elmoManagerFactory.setQueryLanguage(QueryLanguage.SPARQL);
        initialized = true;

        // TODO: this is a hack
        new Thread(new PeriodicDumperRunnable()).start();
    }

    public TweetStoreConnection createConnection() throws TweetStoreException {
        return new TweetStoreConnection(this);
    }

    private void addSeedDataIfEmpty(final Repository repository) throws TweetStoreException {
        try {
            RepositoryConnection rc = repository.getConnection();
            try {
                if (rc.isEmpty()) {
                    LOGGER.info("adding seed data");
                    String baseURI = "http://example.org/bogusBaseURI/";
                    rc.add(TwitLogic.class.getResourceAsStream("namespaces.ttl"), baseURI, RDFFormat.TURTLE);
                    rc.add(TwitLogic.class.getResourceAsStream("twitlogic-void.trig"), baseURI, RDFFormat.TRIG);
                    rc.add(TwitLogic.class.getResourceAsStream("twitlogic-metadata.trig"), baseURI, RDFFormat.TRIG);
                    rc.commit();
                }
            } finally {
                rc.close();
            }
        } catch (IOException e) {
            throw new TweetStoreException(e);
        } catch (RDFParseException e) {
            throw new TweetStoreException(e);
        } catch (RepositoryException e) {
            throw new TweetStoreException(e);
        }
    }

    public Sail getSail() {
        if (!initialized) {
            throw new IllegalStateException("not yet initialized");
        }

        return sail;
    }

    public Repository getRepository() {
        if (!initialized) {
            throw new IllegalStateException("not yet initialized");
        }

        return repository;
    }

    public ElmoModule getElmoModule() {
        if (!initialized) {
            throw new IllegalStateException("not yet initialized");
        }

        return adminElmoModule;
    }

    public ElmoManagerFactory getElmoManagerFactory() {
        return elmoManagerFactory;
    }

    public void shutDown() throws TweetStoreException {
        if (!initialized) {
            throw new IllegalStateException("not yet initialized");
        }

        LOGGER.info("shutting down TwitLogic local store");

        // Note: elmoModule doesn't need to be closed or shutDown.

        try {
            sail.shutDown();
        } catch (SailException e) {
            throw new TweetStoreException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // convenience methods, may be moved ///////////////////////////////////////

    public void dump(final OutputStream out) throws RepositoryException, RDFHandlerException {
        LOGGER.info("dumping triple store to output stream");
        RDFHandler h = Rio.createWriter(RDFFormat.TRIG, out);
        RepositoryConnection rc = getRepository().getConnection();
        try {
            rc.export(h);
        } finally {
            rc.close();
        }
    }

    public void dumpToFile(final File file,
                           final RDFFormat format) throws IOException, RepositoryException, RDFHandlerException {
        LOGGER.info("dumping triple store, using format " + format + " to file: " + file);
        OutputStream out = new FileOutputStream(file);
        try {
            RDFHandler h = Rio.createWriter(format, out);
            RepositoryConnection rc = getRepository().getConnection();
            try {
                rc.export(h);
            } finally {
                rc.close();
            }
        } finally {
            out.close();
        }
    }

    public void dumpToCompressedFile(final File file,
                                     final RDFFormat format) throws IOException, RepositoryException, RDFHandlerException {
        LOGGER.info("dumping compressed triple store to output stream");
        OutputStream out = new FileOutputStream(file);
        try {
            OutputStream gzipOut = new GZIPOutputStream(out);
            try {
                RDFHandler h = Rio.createWriter(format, gzipOut);
                RepositoryConnection rc = getRepository().getConnection();
                try {
                    rc.export(h);
                } finally {
                    rc.close();
                }
            } finally {
                gzipOut.close();
            }
        } finally {
            out.close();
        }
    }

    public void dumpToSparqlUpdateEndpoint(final String endpointURI) throws TweetStoreException {
        LOGGER.info("dumping triple store to SPARUL endpoint: " + endpointURI);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            SparqlUpdateTools.dumpTripleStore(this.getSail(), bos);
        } catch (SailException e) {
            throw new TweetStoreException(e);
        } catch (IOException e) {
            throw new TweetStoreException(e);
        }

        String data = bos.toString();

        // TODO
    }

    ////////////////////////////////////////////////////////////////////////////

    private Sail createSail(final String sailType) throws TweetStoreException {
        System.out.println("creating Sail of type: " + sailType);
        Sail sail;

        if (sailType.equals(MemoryStore.class.getName())) {
            sail = createMemoryStore();
        } else if (sailType.equals(NativeStore.class.getName())) {
            sail = createNativeStore();
        } else {
            throw new TweetStoreException("unhandled Sail type: " + sailType);
        }

        return sail;
    }

    private Sail createMemoryStore() throws TweetStoreException {
        LOGGER.info("instantiating MemoryStore");

        Sail sail = new MemoryStore();
        try {
            sail.initialize();
        } catch (SailException e) {
            throw new TweetStoreException(e);
        }

        return sail;
    }

    private Sail createNativeStore() throws TweetStoreException {
        TypedProperties props = TwitLogic.getConfiguration();
        File dir;

        try {
            dir = props.getFile(TwitLogic.NATIVESTORE_DIRECTORY);
        } catch (PropertyException e) {
            throw new TweetStoreException(e);
        }

        LOGGER.info("instantiating NativeStore in directory: " + dir);
        Sail sail = new NativeStore(dir);
        try {
            sail.initialize();
        } catch (SailException e) {
            throw new TweetStoreException(e);
        }

        return sail;
    }

    private class PeriodicDumperRunnable implements Runnable {
        public void run() {
            while (true) {
                try {
                    File f = new File(TwitLogic.getConfiguration().getFile(TwitLogic.SERVER_STATICCONTENTDIRECTORY),
                            "archive/twitlogic-full.rdf");
                    dumpToFile(f, RDFFormat.RDFXML);
                } catch (Throwable t) {
                    LOGGER.severe("dumper runnable died with error: " + t);
                    t.printStackTrace();
                    return;
                }

                try {
                    Thread.sleep(5 * 60 * 1000);
                } catch (InterruptedException e) {
                    LOGGER.severe("dumper runnable died with error: " + e);
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
