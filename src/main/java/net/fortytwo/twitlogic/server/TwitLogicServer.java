package net.fortytwo.twitlogic.server;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import net.fortytwo.twitlogic.persistence.TweetStore;
import net.fortytwo.twitlogic.server.rewriter.RewriterSail;
import net.fortytwo.twitlogic.server.rewriter.RewritingSchema;
import net.fortytwo.twitlogic.server.rewriter.URIRewriter;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.data.Request;

/**
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:39:31 PM
 */
public class TwitLogicServer {
    public static final String SERVER_ATTR = "server";

    private static final int DEFAULT_PORT = 8182;

    // FIXME: temporary
    private static TwitLogicServer singleton;

    private final SailSelector sailSelector;

    public static TwitLogicServer getWiki(final Context context) {
        return singleton;
    }

    public Sail getSail(final Request request) throws Exception {
        return sailSelector.selectSail(request);
    }

    public TwitLogicServer(final TweetStore store) throws ServerException {
        singleton = this;
        Sail sail = store.getSail();
        final ValueFactory valueFactory = sail.getValueFactory();

        final String serverBaseURI;
        try {
            serverBaseURI = TwitLogic.getConfiguration().getURI(TwitLogic.SERVER_BASEURI).toString();
        } catch (PropertyException e) {
            throw new ServerException(e);
        }

        if (!serverBaseURI.equals(TwitLogic.BASE_URI)) {
            URIRewriter fromStoreRewriter = new URIRewriter() {
                public URI rewrite(final URI original) {
                    if (null == original) {
                        return null;
                    } else {
                        String s = original.toString();
                        return s.startsWith(TwitLogic.BASE_URI)
                                ? valueFactory.createURI(s.replace(TwitLogic.BASE_URI, serverBaseURI))
                                : original;
                    }
                }
            };

            URIRewriter toStoreRewriter = new URIRewriter() {
                public URI rewrite(final URI original) {
                    if (null == original) {
                        return null;
                    } else {
                        String s = original.toString();
                        return s.startsWith(serverBaseURI)
                                ? valueFactory.createURI(s.replace(serverBaseURI, TwitLogic.BASE_URI))
                                : original;
                    }
                }
            };

            RewritingSchema schema = new RewritingSchema();
            schema.setRewriter(RewritingSchema.PartOfSpeech.SUBJECT,
                    RewritingSchema.Action.TO_STORE,
                    toStoreRewriter);
            schema.setRewriter(RewritingSchema.PartOfSpeech.PREDICATE,
                    RewritingSchema.Action.TO_STORE,
                    toStoreRewriter);
            schema.setRewriter(RewritingSchema.PartOfSpeech.OBJECT,
                    RewritingSchema.Action.TO_STORE,
                    toStoreRewriter);
            schema.setRewriter(RewritingSchema.PartOfSpeech.GRAPH,
                    RewritingSchema.Action.TO_STORE,
                    toStoreRewriter);
            schema.setRewriter(RewritingSchema.PartOfSpeech.SUBJECT,
                    RewritingSchema.Action.FROM_STORE,
                    fromStoreRewriter);
            schema.setRewriter(RewritingSchema.PartOfSpeech.PREDICATE,
                    RewritingSchema.Action.FROM_STORE,
                    fromStoreRewriter);
            schema.setRewriter(RewritingSchema.PartOfSpeech.OBJECT,
                    RewritingSchema.Action.FROM_STORE,
                    fromStoreRewriter);
            schema.setRewriter(RewritingSchema.PartOfSpeech.GRAPH,
                    RewritingSchema.Action.FROM_STORE,
                    fromStoreRewriter);

            sail = new RewriterSail(sail, schema);
        }

        final Sail selectedSail = sail;

        sailSelector = new SailSelector() {
            public Sail selectSail(final Request request) throws Exception {
                return selectedSail;
            }
        };

        // Create a new Restlet component and add a HTTP server connector to it
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);

        component.getDefaultHost().getContext().getAttributes().put(SERVER_ATTR, this);
        component.getDefaultHost().attach(new RootApplication());
        try {
            component.start();
        } catch (Exception e) {
            throw new ServerException(e);
        }
    }
}
