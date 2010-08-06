package net.fortytwo.twitlogic.persistence.sail;

import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGServer;
import net.fortytwo.sesametools.replay.RecorderSail;
import net.fortytwo.sesametools.reposail.RepositorySail;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.SailFactory;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Jul 9, 2010
 * Time: 8:39:35 PM
 */
public class NewAllegroSailFactory extends SailFactory {
    private static final Logger LOGGER = TwitLogic.getLogger(NewAllegroSailFactory.class);
    private final boolean enableLogging;

    /**
     * @param conf configuration properties for the Sail
     */
    public NewAllegroSailFactory(final TypedProperties conf,
                                 final boolean enableLogging) {
        super(conf);
        this.enableLogging = enableLogging;
    }

    public Sail makeSail() throws SailException, PropertyException {
        String host = conf.getString(TwitLogic.ALLEGROSAIL_HOST);
        String name = conf.getString(TwitLogic.ALLEGROSAIL_NAME);
        String catName = conf.getString(TwitLogic.ALLEGROSAIL_CATALOG_NAME);
        String userName = conf.getString(TwitLogic.ALLEGROSAIL_USERNAME);
        String password = conf.getString(TwitLogic.ALLEGROSAIL_PASSWORD);

        LOGGER.info("connecting to AllegroGraph triple store \"" + name + "\""
                + " in catalog \"" + catName + "\""
                + " on host " + host);
        AGServer server = new AGServer(host, userName, password);

        AGCatalog cat = new AGCatalog(server, catName);

        AGRepository repo = new AGRepository(cat, name);

        RepositorySail sail = new RepositorySail(repo);
        sail.disableInference();
        sail.initialize();

        if (enableLogging) {
            File logFile = new File("/tmp/twitlogic-ag-sail.log");
            Sail recorderSail;
            try {
                recorderSail = new RecorderSail(sail, new FileOutputStream(logFile));
            } catch (FileNotFoundException e) {
                throw new SailException(e);
            }

            //return sail;
            return recorderSail;
        } else {
            return sail;
        }
    }
}
