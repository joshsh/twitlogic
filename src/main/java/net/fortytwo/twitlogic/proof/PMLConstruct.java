package net.fortytwo.twitlogic.proof;

import net.fortytwo.twitlogic.flow.Handler;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;

import java.util.Random;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 3:11:45 AM
 */
public abstract class PMLConstruct {
    private static final Random RANDOM = new Random();

    protected final Resource self;
    protected final RDFizerContext context;

    protected abstract void handleStatements(Handler<Statement, RDFizerException> handler) throws RDFizerException;

    protected PMLConstruct(final RDFizerContext context) {
        this.context = context;
        // TODO: improve this
        this.self = context.getValueFactory().createURI(
                context.getBaseURI() + RANDOM.nextInt(Integer.MAX_VALUE));
    }

    protected class RDFizerException extends Exception {
        public RDFizerException(final Throwable cause) {
            super(cause);
        }
    }

    public class RDFizerContext {
        private final String baseURI;
        private final ValueFactory valueFactory;
        private final Resource namedGraph;

        public RDFizerContext(final String baseURI,
                              final Resource namedGraph,
                              final ValueFactory valueFactory) {
            this.baseURI = baseURI;
            this.namedGraph = namedGraph;
            this.valueFactory = valueFactory;
        }

        public String getBaseURI() {
            return baseURI;
        }

        public ValueFactory getValueFactory() {
            return valueFactory;
        }

        public Resource getNamedGraph() {
            return namedGraph;
        }
    }
}
