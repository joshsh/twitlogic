package net.fortytwo.twitlogic.persistence;

import com.knowledgereefsystems.agsail.AllegroSail;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.beans.Agent;
import net.fortytwo.twitlogic.persistence.beans.Document;
import net.fortytwo.twitlogic.persistence.beans.Feature;
import net.fortytwo.twitlogic.persistence.beans.Graph;
import net.fortytwo.twitlogic.persistence.beans.Image;
import net.fortytwo.twitlogic.persistence.beans.MicroblogPost;
import net.fortytwo.twitlogic.persistence.beans.Point;
import net.fortytwo.twitlogic.persistence.beans.SpatialThing;
import net.fortytwo.twitlogic.persistence.beans.User;
import net.fortytwo.twitlogic.persistence.sail.AllegroSailFactory;
import net.fortytwo.twitlogic.persistence.sail.MemoryStoreFactory;
import net.fortytwo.twitlogic.persistence.sail.NativeStoreFactory;
import net.fortytwo.twitlogic.util.SparqlUpdateTools;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.concepts.owl.ObjectProperty;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.ElmoManagerFactory;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.model.Resource;
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
    private static final Logger LOGGER = TwitLogic.getLogger(TweetStore.class);

    private final Sail sail;
    private final TypedProperties configuration;
    private Repository repository;
    private ElmoModule adminElmoModule;
    private SesameManagerFactory elmoManagerFactory;
    private boolean initialized = false;

    /**
     * The Sesame storage and inference layer (Sail) will be constructed according to configuration properties.
     * 
     * @throws TweetStoreException
     */
    public TweetStore() throws TweetStoreException {
        configuration = TwitLogic.getConfiguration();
        String sailType;
        try {
            sailType = configuration.getString(TwitLogic.SAIL_CLASS);
        } catch (PropertyException e) {
            throw new TweetStoreException(e);
        }
        sail = createSail(sailType, configuration);
    }

    /**
     *
     * @param sail a Sesame storage and inference layer
     */
    public TweetStore(final Sail sail) {
        this.sail = sail;
        this.configuration = TwitLogic.getConfiguration();
    }

    public void initialize() throws TweetStoreException {
        if (initialized) {
            throw new IllegalStateException("store has already been initialized");
        }

        LOGGER.info("initializing TwitLogic local store");

        repository = new SailRepository(sail);
        refreshCoreMetadata(repository);

        // Elmo setup.
        adminElmoModule = new ElmoModule();
        adminElmoModule.setGraph(null);  // for TwitLogic.AUTHORITATIVE_GRAPH
        adminElmoModule.addConcept(Thing.class);
        adminElmoModule.addConcept(ObjectProperty.class);  // Dunno why this is necessary, but Elmo logs warnings without it

        // TwitLogic-specific classes
        adminElmoModule.addConcept(Agent.class);
        adminElmoModule.addConcept(org.openrdf.concepts.rdfs.Class.class);
        adminElmoModule.addConcept(Document.class);
        adminElmoModule.addConcept(Feature.class);
        adminElmoModule.addConcept(Graph.class);
        adminElmoModule.addConcept(Image.class);
        adminElmoModule.addConcept(MicroblogPost.class);
        adminElmoModule.addConcept(Point.class);
        adminElmoModule.addConcept(SpatialThing.class);
        adminElmoModule.addConcept(User.class);

        elmoManagerFactory
                = new SesameManagerFactory(adminElmoModule, repository);
        elmoManagerFactory.setQueryLanguage(QueryLanguage.SPARQL);

        addPeriodicDump();

        initialized = true;
    }

    private void addPeriodicDump() throws TweetStoreException {
        TypedProperties conf = TwitLogic.getConfiguration();
        try {
            File file = conf.getFile(TwitLogic.DUMP_FILE, null);
            if (null == file) {
                LOGGER.info("no dump file specified. Periodic data dumps will not be generated.");
            } else {
                long interval = conf.getLong(TwitLogic.DUMP_INTERVAL, -1);
                if (-1 == interval) {
                    LOGGER.warning("no dump interval specified. Periodic data dumps will not be generated.");
                } else {
                    boolean compressed = false;
                    String s = file.getName();
                    if (s.endsWith(".gz")) {
                        compressed = true;
                        s = s.substring(0, s.length() - ".gz".length());
                    }

                    int i = s.lastIndexOf('.');
                    if (i <= 0) {
                        LOGGER.warning("dump file name could not be parsed. Periodic data dumps will not be generated.");
                    } else {
                        String ext = s.substring(i + 1);
                        RDFFormat format = SesameTools.rdfFormatByExtension(ext);
                        if (null == format) {
                            LOGGER.warning("dump file format not recognized. Periodic data dumps will not be generated.");
                        } else {
                            try {
                                try {
                                    new Thread(new PeriodicDumpfileGenerator(this, file, format, compressed, interval)).start();
                                } catch (IOException e) {
                                    throw new TweetStoreException(e);
                                }
                            } catch (PropertyException e) {
                                throw new TweetStoreException(e);
                            }
                        }
                    }
                }
            }
        } catch (PropertyException e) {
            throw new TweetStoreException(e);
        }
    }

    public TweetStoreConnection createConnection() throws TweetStoreException {
        return new TweetStoreConnection(this);
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
//        new Exception().printStackTrace();

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
        RDFFormat format = RDFFormat.TRIG;
        LOGGER.info("dumping triple store in format " + format.getName() + " to output stream");
        RDFHandler h = Rio.createWriter(format, out);
        RepositoryConnection rc = getRepository().getConnection();
        try {
            rc.export(h);
        } finally {
            rc.close();
        }
    }

    public void dumpToFile(final File file,
                           final RDFFormat format) throws IOException, RepositoryException, RDFHandlerException {
        LOGGER.info("dumping triple store in format " + format.getName() + " to file: " + file);
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
        LOGGER.info("dumping compressed triple store in format " + format.getName() + " to file: " + file);
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

    public void clear() throws TweetStoreException {
        try {
            RepositoryConnection rc = repository.getConnection();
            try {
                rc.clear();
                rc.commit();
            } finally {
                rc.close();
            }
        } catch (RepositoryException e) {
            throw new TweetStoreException(e);
        }
    }

    public void load(final File file,
                     final RDFFormat format) throws TweetStoreException {
        try {
            RepositoryConnection rc = repository.getConnection();
            try {
                try {
                    rc.add(file, "http://example.org/baseURI", format);
                } catch (IOException e) {
                    throw new TweetStoreException(e);
                } catch (RDFParseException e) {
                    throw new TweetStoreException(e);
                }

                rc.commit();
            } finally {
                rc.close();
            }
        } catch (RepositoryException e) {
            throw new TweetStoreException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private Sail createSail(final String sailType,
                            final TypedProperties props) throws TweetStoreException {
        System.out.println("creating Sail of type: " + sailType);
        Sail sail;
        SailFactory factory;

        if (sailType.equals(MemoryStore.class.getName())) {
            factory = new MemoryStoreFactory(props);
        } else if (sailType.equals(NativeStore.class.getName())) {
            factory = new NativeStoreFactory(props);
        } else if (sailType.equals(AllegroSail.class.getName())) {
            factory = new AllegroSailFactory(props);
        } else {
            throw new TweetStoreException("unhandled Sail type: " + sailType);
        }

        try {
            return factory.makeSail();
        } catch (SailException e) {
            throw new TweetStoreException(e);
        } catch (PropertyException e) {
            throw new TweetStoreException(e);
        }
    }

    private void refreshCoreMetadata(final Repository repository) throws TweetStoreException {
        LOGGER.info("adding/refreshing core metadata");

        try {
            RepositoryConnection rc = repository.getConnection();
            try {

                rc.remove((Resource) null, null, null, TwitLogic.CORE_GRAPH);
                rc.clearNamespaces();

                String baseURI = "http://example.org/baseURI/";
                rc.add(TwitLogic.class.getResourceAsStream("namespaces.ttl"),
                        baseURI, RDFFormat.TURTLE, TwitLogic.CORE_GRAPH);
                rc.add(TwitLogic.class.getResourceAsStream("twitlogic-void.ttl"),
                        baseURI, RDFFormat.TURTLE, TwitLogic.CORE_GRAPH);
                rc.add(TwitLogic.class.getResourceAsStream("twitterplaces.ttl"),
                        baseURI, RDFFormat.TURTLE, TwitLogic.CORE_GRAPH);
                
                rc.commit();
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
}
