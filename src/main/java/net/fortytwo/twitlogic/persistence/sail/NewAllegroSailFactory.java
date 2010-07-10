package net.fortytwo.twitlogic.persistence.sail;

import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGServer;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.persistence.SailFactory;
import net.fortytwo.twitlogic.persistence.sail.reposail.RepositorySail;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import org.openrdf.repository.Repository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

/**
 * User: josh
 * Date: Jul 9, 2010
 * Time: 8:39:35 PM
 */
public class NewAllegroSailFactory extends SailFactory {
    /**
     * @param conf configuration properties for the Sail
     */
    public NewAllegroSailFactory(final TypedProperties conf) {
        super(conf);
    }

    public Sail makeSail() throws SailException, PropertyException {
        String host = conf.getString(TwitLogic.ALLEGROSAIL_HOST);
        String name = conf.getString(TwitLogic.ALLEGROSAIL_NAME);
        String catName = conf.getString(TwitLogic.ALLEGROSAIL_CATALOG_NAME);
        String userName = conf.getString(TwitLogic.ALLEGROSAIL_USERNAME);
        String password = conf.getString(TwitLogic.ALLEGROSAIL_PASSWORD);

        AGServer server = new AGServer(host, userName, password);

        AGCatalog cat = new AGCatalog(server, catName);

        Repository repo = new AGRepository(cat, name);

        Sail sail = new RepositorySail(repo);
        sail.initialize();

        return sail;
    }
}
