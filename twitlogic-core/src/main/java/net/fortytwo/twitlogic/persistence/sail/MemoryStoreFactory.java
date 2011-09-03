package net.fortytwo.twitlogic.persistence.sail;

import net.fortytwo.twitlogic.persistence.SailFactory;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import net.fortytwo.twitlogic.TwitLogic;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class MemoryStoreFactory extends SailFactory {

    private static final Logger LOGGER = TwitLogic.getLogger(MemoryStoreFactory.class);

    public MemoryStoreFactory(final TypedProperties conf) {
        super(conf);
    }

    public Sail makeSail() throws SailException, PropertyException {
        LOGGER.info("instantiating MemoryStore");

        Sail sail = new MemoryStore();
        sail.initialize();
        return sail;
    }
}
