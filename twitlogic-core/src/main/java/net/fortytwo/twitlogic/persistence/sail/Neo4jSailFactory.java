package net.fortytwo.twitlogic.persistence.sail;

import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.SailFactory;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Neo4jSailFactory extends SailFactory {

    private static final Logger LOGGER = TwitLogic.getLogger(Neo4jSailFactory.class);
    private static final long CHUNK_SIZE = 1000;

    public Neo4jSailFactory(final TypedProperties config) {
        super(config);
    }

    public Sail makeSail() throws SailException, PropertyException {
        File dir = conf.getFile(TwitLogic.NEO4J_DIRECTORY);

        LOGGER.info("instantiating GraphSail-on-Neo4j in directory: " + dir);
        Neo4jGraph graph = new Neo4jGraph(dir.getAbsolutePath());
        //graph.setMaxBufferSize(1);
        Sail sail = new GraphSail(graph);
        sail.initialize();

        File dump = conf.getFile("dump", null);
        if (null != dump) {
            LOGGER.info("loading from dump file: " + dump);

            try {
                SailConnection sc = sail.getConnection();
                try {
                    sc.begin();
                    RDFParser parser = Rio.createParser(RDFFormat.NQUADS);
                    parser.setRDFHandler(new StatementAdder(sc));
                    InputStream is = new FileInputStream(dump);
                    try {
                        parser.parse(is, "");
                    } finally {
                        is.close();
                    }
                    sc.commit();
		sc.begin();
                } finally {
                    sc.rollback();
                    sc.close();
                }
            } catch (Throwable t) {
                LOGGER.severe("exception occurred while loading: " + t);
                t.printStackTrace(System.err);
                System.exit(1);
            }

            LOGGER.info("done loading");
        }

        return sail;
    }

    private static class StatementAdder implements RDFHandler {
        private final SailConnection c;
        private long count = 0;

        public StatementAdder(SailConnection c) {
            this.c = c;
        }

        public void startRDF() throws RDFHandlerException {
        }

        public void endRDF() throws RDFHandlerException {
            try {
                c.commit();
		c.begin();
            } catch (SailException e) {
                throw new RDFHandlerException(e);
            }
        }

        public void handleNamespace(String s, String s1) throws RDFHandlerException {
        }

        public void handleStatement(Statement s) throws RDFHandlerException {
            try {
                c.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext());
                if (0 == ++count % CHUNK_SIZE) {
                    c.commit();
		    c.begin();
                }
            } catch (SailException e) {
                throw new RDFHandlerException(e);
            }
        }

        public void handleComment(String s) throws RDFHandlerException {
        }
    }
}
