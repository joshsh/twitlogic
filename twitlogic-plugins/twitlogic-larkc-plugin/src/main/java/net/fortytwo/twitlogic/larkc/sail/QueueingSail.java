package net.fortytwo.twitlogic.larkc.sail;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.helpers.SailWrapper;

/**
 * User: josh
 * Date: 1/8/11
 * Time: 7:43 AM
 */
public class QueueingSail extends SailWrapper {
    private final SailConnectionListener listener;

    public QueueingSail(final Sail baseSail,
                        SailConnectionListener listener) {
        super(baseSail);
        this.listener = listener;
    }

    @Override
    public SailConnection getConnection() throws SailException {
        return new QueueingSailConnection(this.getBaseSail().getConnection(), listener, this.getValueFactory());
    }
}
