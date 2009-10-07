package net.fortytwo.twitlogic.server;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.Directory;
import org.restlet.data.Reference;

import java.io.File;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.properties.PropertyException;

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
        router.attach("/resource/", InformationResource.class);

        // Return the root router
        return router;
    }
}

