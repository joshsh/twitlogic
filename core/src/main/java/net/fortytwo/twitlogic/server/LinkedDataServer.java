package net.fortytwo.twitlogic.server;

import net.fortytwo.sesametools.mappingsail.MappingSail;
import net.fortytwo.sesametools.mappingsail.MappingSchema;
import net.fortytwo.sesametools.mappingsail.RewriteRule;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.properties.PropertyException;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;

/**
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:39:31 PM
 */
public class LinkedDataServer {
    public static final String SERVER_ATTR = "server";

    private static final int DEFAULT_PORT = 8182;

    private final Sail sail;
    private final URI datasetURI;

    public Sail getSail() throws Exception {
        return sail;
    }

    public URI getDatasetURI() {
        return datasetURI;
    }

    public static LinkedDataServer getServer(Context context) {
        Object o = context.getAttributes().get(SERVER_ATTR);
        if (o instanceof LinkedDataServer) {
            return (LinkedDataServer) o;
        } else {
            throw new IllegalStateException();
        }
    }

    public LinkedDataServer(final Sail baseSail) throws ServerException {

        final ValueFactory valueFactory = baseSail.getValueFactory();

        final String serverBaseURI;
        final int serverPort;
        try {
            serverBaseURI = TwitLogic.getConfiguration().getURI(TwitLogic.SERVER_BASEURI).toString();
            serverPort = TwitLogic.getConfiguration().getInt(TwitLogic.SERVER_PORT, DEFAULT_PORT);
        } catch (PropertyException e) {
            throw new ServerException(e);
        }

        if (!serverBaseURI.equals(TwitLogic.BASE_URI)) {
            RewriteRule outboundRewriter = new RewriteRule() {
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

            RewriteRule inboundRewriter = new RewriteRule() {
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

            MappingSchema schema = new MappingSchema();
            schema.setRewriter(MappingSchema.Direction.INBOUND, inboundRewriter);
            schema.setRewriter(MappingSchema.Direction.OUTBOUND, outboundRewriter);
            this.sail = new MappingSail(baseSail, schema);

            datasetURI = outboundRewriter.rewrite(this.sail.getValueFactory().createURI(TwitLogic.TWITLOGIC_DATASET));
        } else {
            this.sail = baseSail;
            datasetURI = this.sail.getValueFactory().createURI(TwitLogic.TWITLOGIC_DATASET);
        }

        // Create a new Restlet component and add a HTTP server connector to it
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, serverPort);
        //component.getServers().add(Protocol.FILE);
        component.getClients().add(Protocol.FILE);

        component.getDefaultHost().getContext().getAttributes().put(SERVER_ATTR, this);
        component.getDefaultHost().attach(new RootApplication());

        try {
            component.start();
        } catch (Exception e) {
            throw new ServerException(e);
        }
    }
}
