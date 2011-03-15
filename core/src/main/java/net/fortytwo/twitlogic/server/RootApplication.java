package net.fortytwo.twitlogic.server;

import edu.rpi.tw.twctwit.query.RelatedHashtagsResource;
import edu.rpi.tw.twctwit.query.RelatedTweetsResource;
import edu.rpi.tw.twctwit.query.SparqlResource;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import org.restlet.Application;
import org.restlet.Context;
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
        Router router = new Router(getContext());

        router.attach("/", new Directory(getContext(), "file://" + staticContentDir + "/"));

        for (TwitLogic.ResourceType t : TwitLogic.ResourceType.values()) {
            if (!t.getUriPath().equals("graph")) {
                router.attach("/" + t.getUriPath() + "/", WebResource.class);
            }
        }
        router.attach("/graph/", GraphResource.class);

        router.attach("/sparql", SparqlResource.class);
        router.attach("/stream/relatedTweets", RelatedTweetsResource.class);
        router.attach("/stream/relatedTags", RelatedHashtagsResource.class);

        // Return the root router
        return router;
    }
}
