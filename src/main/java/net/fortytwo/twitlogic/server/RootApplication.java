package net.fortytwo.twitlogic.server;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import org.restlet.Application;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.Router;

import java.io.File;

public class RootApplication extends Application {
    private final File staticContentDir;
    public RootApplication() throws ServerException {
        super();

        try {
            staticContentDir = TwitLogic.getConfiguration().getFile(TwitLogic.SERVER_STATICCONTENTDIRECTORY);
        } catch (PropertyException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public Restlet createRoot() {
        // Create a root router
        Router router = new Router(getContext());

        router.attach("/", new Directory(getContext(), "file://" + staticContentDir + "/"));

        for (TwitLogic.ResourceType t : TwitLogic.ResourceType.values()) {
            router.attach("/" + t.getUriPath() + "/", WebResource.class);
        }

        // Return the root router
        return router;
    }
}

