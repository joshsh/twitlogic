package net.fortytwo.twitlogic.persistence.sail;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.SailFactory;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.File;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: May 18, 2010
 * Time: 5:26:47 PM
 */
public class NativeStoreFactory extends SailFactory {

    private static final Logger LOGGER = TwitLogic.getLogger(NativeStoreFactory.class);

    public NativeStoreFactory(final TypedProperties conf) {
        super(conf);
    }

    public Sail makeSail() throws SailException, PropertyException {
        File dir = conf.getFile(TwitLogic.NATIVESTORE_DIRECTORY);

        LOGGER.info("instantiating NativeStore in directory: " + dir);
        Sail sail = new NativeStore(dir);
        sail.initialize();

        return sail;
    }
}
