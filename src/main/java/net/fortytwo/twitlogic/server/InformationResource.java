package net.fortytwo.twitlogic.server;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.twitlogic.TwitLogic;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:55:27 PM
 */
public class InformationResource extends Resource {
    private static final Logger LOGGER
            = TwitLogic.getLogger(InformationResource.class);

    protected URI selfUri;
    protected Sail sail;

    public InformationResource(final Context context, final Request request,
                               final Response response) throws Exception {
        super(context, request, response);

        sail = TwitLogicServer.getWiki(context).getSail(request);

        selfUri = sail.getValueFactory().createURI(
                request.getResourceRef().toString());
        System.out.println("selfUri = " + selfUri);
        getVariants().addAll(RDFStuff.getRDFVariants());
        System.out.println("    set variants");
    }

    public boolean allowDelete() {
        return true;
    }

    public boolean allowGet() {
        return true;
    }

    public boolean allowPost() {
        return true;
    }

    public boolean allowPut() {
        return true;
    }

    @Override
    public Representation represent(final Variant variant) {
        MediaType type = variant.getMediaType();
        RDFFormat format;
        format = RDFStuff.findRdfFormat(type);

        if (null != format) {
            return getRDFRepresentation(format);
        }

        return null;
    }

    private Representation getRDFRepresentation(final RDFFormat format) {
        try {
            Collection<Namespace> namespaces = new LinkedList<Namespace>();
            Collection<Statement> statements = new LinkedList<Statement>();

            SailConnection sc = sail.getConnection();
            try {
                // Select outbound statements
                CloseableIteration<? extends Statement, SailException> stIter
                        = sc.getStatements(selfUri, null, null, false);
                try {
                    while (stIter.hasNext()) {
                        statements.add(stIter.next());
                    }
                } finally {
                    stIter.close();
                }

                // Select inbound statements
                stIter = sc.getStatements(null, null, selfUri, false);
                try {
                    while (stIter.hasNext()) {
                        statements.add(stIter.next());
                    }
                } finally {
                    stIter.close();
                }

                // Select namespaces, for human-friendliness
                CloseableIteration<? extends Namespace, SailException> nsIter
                        = sc.getNamespaces();
                try {
                    while (nsIter.hasNext()) {
                        namespaces.add(nsIter.next());
                    }
                } finally {
                    nsIter.close();
                }
            } finally {
                sc.close();
            }
            return new RDFRepresentation(statements, namespaces, format);

        } catch (Throwable t) {
            // TODO: put this in the logger message
            t.printStackTrace();
            
            LOGGER.log(Level.WARNING, "failed to create RDF representation", t);
            return null;
        }
    }
}

