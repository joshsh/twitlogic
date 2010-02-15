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

        // FIXME: use constants defined in TwitLogic.java
        router.attach("/dataset/", WebResource.class);
        router.attach("/graph/", WebResource.class);
        router.attach("/hashtag/", WebResource.class);
        router.attach("/location/", WebResource.class);
        router.attach("/person/", WebResource.class);
        router.attach("/post/twitter/", WebResource.class);
        router.attach("/user/twitter/", WebResource.class);

        // Return the root router
        return router;
    }
}

