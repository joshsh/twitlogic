package net.fortytwo.twitlogic.server;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;

public class RootApplication extends Application {
    public RootApplication() {
        super();
    }

    @Override
    public Restlet createRoot() {
        // Create a root router
        Router router = new Router(getContext());
        
        router.attach("/", InformationResource.class);

        // Return the root router
        return router;
    }
}

