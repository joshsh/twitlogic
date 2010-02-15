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
        //router.attach("/resource/", InformationResource.class);

        // FIXME: use constants defined in TwitLogic.java
        router.attach("/graph/", InformationResource.class);
        router.attach("/hashtag/", InformationResource.class);
        router.attach("/location/", InformationResource.class);
        router.attach("/person/", InformationResource.class);
        router.attach("/post/twitter/", InformationResource.class);
        router.attach("/user/twitter/", InformationResource.class);

        // Return the root router
        return router;
    }
}

