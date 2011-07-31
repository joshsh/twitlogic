package net.fortytwo.twitlogic.larkc.sail;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

/**
 * User: josh
 * Date: 1/8/11
 * Time: 7:44 AM
 */
public class QueueingSailConnection extends SailConnectionWrapper {
    private static final Resource[] DEFAULT_CONTEXT = {null};

    private final SailConnectionListener listener;
    private final ValueFactory valueFactory;

    public QueueingSailConnection(final SailConnection wrappedCon,
                                  final SailConnectionListener listener,
                                  final ValueFactory valueFactory) {
        super(wrappedCon);
        this.listener = listener;
        this.valueFactory = valueFactory;
    }

    @Override
    public void addStatement(Resource resource, URI uri, Value value, Resource... resources) throws SailException {
        if (0 == resources.length) {
            resources = DEFAULT_CONTEXT;
        }

        for (Resource c : resources) {
            Statement st = valueFactory.createStatement(resource, uri, value, c);
            listener.statementAdded(st);
        }

        // Elmo needs to be able to retrieve previously added statements, even within a single transaction.
        this.getWrappedConnection().addStatement(resource, uri, value, resources);
    }
}
