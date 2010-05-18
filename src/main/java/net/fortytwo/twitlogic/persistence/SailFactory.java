package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

/**
 * User: josh
 * Date: May 18, 2010
 * Time: 5:23:16 PM
 */
public abstract class SailFactory {
    protected final TypedProperties conf;

    /**
     *
     * @param conf configuration properties for the Sail
     */
    public SailFactory(final TypedProperties conf) {
        this.conf = conf;
    }

    /**
     *
     * @return an initialized Sail
     * @throws SailException if there is a problem creating the Sail
     * @throws PropertyException if there is a problem reading the configuration
     */
    public abstract Sail makeSail() throws SailException, PropertyException;
}
