package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

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
    private boolean initialized = false;

    public static TweetStore getDefaultStore() throws TwitLogicStoreException {
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

    public class TwitLogicStoreException extends Exception {
        public TwitLogicStoreException(final Throwable cause) {
            super(cause);
        }

        public TwitLogicStoreException(final String msg) {
            super(msg);
        }
    }

    private TweetStore() throws TwitLogicStoreException {
        TypedProperties props = TwitLogic.getConfiguration();
        String sailType;
        try {
            sailType = props.getString(TwitLogic.SAIL_CLASS);
        } catch (PropertyException e) {
            throw new TwitLogicStoreException(e);
        }
        sail = createSail(sailType);
    }

    public TweetStore(final Sail sail) {
        this.sail = sail;
    }

    public void initialize() throws TwitLogicStoreException {
        if (initialized) {
            throw new IllegalStateException("store has already been initialized");
        }

        LOGGER.info("initializing TwitLogic local store");

        repository = new SailRepository(sail);
        addSeedDataIfEmpty(repository);

        initialized = true;
    }

    private void addSeedDataIfEmpty(final Repository repository) throws TwitLogicStoreException {
        try {
            RepositoryConnection rc = repository.getConnection();
            try {
                if (rc.isEmpty()) {
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
            throw new TwitLogicStoreException(e);
        } catch (RDFParseException e) {
            throw new TwitLogicStoreException(e);
        } catch (RepositoryException e) {
            throw new TwitLogicStoreException(e);
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

    public void shutDown() throws TwitLogicStoreException {
        if (!initialized) {
            throw new IllegalStateException("not yet initialized");
        }

        LOGGER.info("shutting down TwitLogic local store");

        // Note: elmoModule doesn't need to be closed or shutDown.

        try {
            sail.shutDown();
        } catch (SailException e) {
            throw new TwitLogicStoreException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // convenience methods, may be moved ///////////////////////////////////////

    public void dump(final OutputStream out) throws RepositoryException, RDFHandlerException {
        RDFHandler h = Rio.createWriter(RDFFormat.TRIG, out);
        RepositoryConnection rc = getRepository().getConnection();
        try {
            rc.export(h);
        } finally {
            rc.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private Sail createSail(final String sailType) throws TwitLogicStoreException {
        System.out.println("creating Sail of type: " + sailType);
        Sail sail;

        if (sailType.equals(MemoryStore.class.getName())) {
            sail = createMemoryStore();
        } else if (sailType.equals(NativeStore.class.getName())) {
            sail = createNativeStore();
        } else {
            throw new TwitLogicStoreException("unhandled Sail type: " + sailType);
        }

        return sail;
    }

    private Sail createMemoryStore() throws TwitLogicStoreException {
        LOGGER.info("instantiating MemoryStore");

        Sail sail = new MemoryStore();
        try {
            sail.initialize();
        } catch (SailException e) {
            throw new TwitLogicStoreException(e);
        }

        return sail;
    }

    private Sail createNativeStore() throws TwitLogicStoreException {
        TypedProperties props = TwitLogic.getConfiguration();
        File dir;

        try {
            dir = props.getFile(TwitLogic.NATIVESTORE_DIRECTORY);
        } catch (PropertyException e) {
            throw new TwitLogicStoreException(e);
        }

        LOGGER.info("instantiating NativeStore in directory: " + dir);
        Sail sail = new NativeStore(dir);
        try {
            sail.initialize();
        } catch (SailException e) {
            throw new TwitLogicStoreException(e);
        }

        return sail;
    }
}
