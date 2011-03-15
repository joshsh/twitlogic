package net.fortytwo.twitlogic.server;

import net.fortytwo.sesametools.mappingsail.MappingSail;
import net.fortytwo.sesametools.mappingsail.MappingSchema;
import net.fortytwo.sesametools.mappingsail.RewriteRule;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;

/**
 * A RESTful web service which publishes the contents of a Sail data store as Linked Data.
 * <p/>
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:39:31 PM
 */
public class LinkedDataServer {
    public static final String SERVER_ATTR = "linked-data-server";

    private final Sail sail;
    private URI datasetURI;

    /**
     * @param baseSail        the data store published by this server
     * @param internalBaseURI the base URI of resources within the data store
     * @param externalBaseURI the base URI of resources as they are to be seen in the Linked Data
     * @param serverPort      the TCP port through which to make the data accessible
     * @throws ServerException if the server cannot be instantiated
     */
    public LinkedDataServer(final Sail baseSail,
                            final String internalBaseURI,
                            final String externalBaseURI,
                            final int serverPort) throws ServerException {
        this(baseSail, internalBaseURI, externalBaseURI, serverPort, null);
    }

    /**
     * @param baseSail        the data store published by this server
     * @param internalBaseURI the base URI of resources within the data store
     * @param externalBaseURI the base URI of resources as they are to be seen in the Linked Data
     * @param serverPort      the TCP port through which to make the data accessible
     * @param dataset         the URI of the data set to be published.
     *                        This allows resource descriptions to be associated with metadata about the data set which contains them.
     * @throws ServerException if the server cannot be instantiated
     */
    public LinkedDataServer(final Sail baseSail,
                            final String internalBaseURI,
                            final String externalBaseURI,
                            final int serverPort,
                            final String dataset) throws ServerException {
        final ValueFactory vf = baseSail.getValueFactory();

        if (!internalBaseURI.equals(externalBaseURI)) {
            RewriteRule outboundRewriter = new RewriteRule() {
                public URI rewrite(final URI original) {
                    if (null == original) {
                        return null;
                    } else {
                        String s = original.stringValue();
                        return s.startsWith(externalBaseURI)
                                ? vf.createURI(s.replace(externalBaseURI, internalBaseURI))
                                : original;
                    }
                }
            };

            RewriteRule inboundRewriter = new RewriteRule() {
                public URI rewrite(final URI original) {
                    if (null == original) {
                        return null;
                    } else {
                        String s = original.stringValue();
                        return s.startsWith(internalBaseURI)
                                ? vf.createURI(s.replace(internalBaseURI, externalBaseURI))
                                : original;
                    }
                }
            };

            MappingSchema schema = new MappingSchema();
            schema.setRewriter(MappingSchema.Direction.INBOUND, inboundRewriter);
            schema.setRewriter(MappingSchema.Direction.OUTBOUND, outboundRewriter);
            this.sail = new MappingSail(baseSail, schema);

            datasetURI = outboundRewriter.rewrite(vf.createURI(dataset));
        } else {
            this.sail = baseSail;
            datasetURI = vf.createURI(dataset);
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

    /**
     * @return the data store published by this server
     */
    public Sail getSail() {
        return sail;
    }

    /**
     * @return the internal URI for the data set published by this server
     */
    public URI getDatasetURI() {
        return datasetURI;
    }

    /**
     * @param context the current Restlet context
     * @return the Linked Data server contained in the given context
     */
    public static LinkedDataServer getServer(Context context) {
        Object o = context.getAttributes().get(SERVER_ATTR);
        if (o instanceof LinkedDataServer) {
            return (LinkedDataServer) o;
        } else {
            throw new IllegalStateException();
        }
    }
}
