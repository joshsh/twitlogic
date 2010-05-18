package net.fortytwo.twitlogic.persistence.sail;

import com.knowledgereefsystems.agsail.AllegroSail;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.SailFactory;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

import java.io.File;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: May 18, 2010
 * Time: 5:27:09 PM
 */
public class AllegroSailFactory extends SailFactory {

    private static final Logger LOGGER = TwitLogic.getLogger(AllegroSailFactory.class);

    /**
     * @param conf configuration properties for the Sail
     */
    public AllegroSailFactory(final TypedProperties conf) {
        super(conf);
    }

    public Sail makeSail() throws SailException, PropertyException {
        String host = conf.getString(TwitLogic.ALLEGROSAIL_HOST);
        int port = conf.getInt(TwitLogic.ALLEGROSAIL_PORT);
        boolean start = conf.getBoolean(TwitLogic.ALLEGROSAIL_START);
        String name = conf.getString(TwitLogic.ALLEGROSAIL_NAME);
        File directory = conf.getFile(TwitLogic.ALLEGROSAIL_DIRECTORY);

        LOGGER.info((start ? "starting" : "connecting to") + " AllegroGraph triple store in " + directory + "/" + name + " at " + host + ":" + port);

        Sail sail = new AllegroSail(host, port, start, name, directory, 0, 0, false, false);

        sail.initialize();

        return sail;
    }
}
