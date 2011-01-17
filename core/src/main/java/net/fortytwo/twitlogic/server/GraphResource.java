package net.fortytwo.twitlogic.server;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.vocabs.FOAF;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Graph resources are information resources which (in the present schema) do not use suffixes identifying the RDF
 * format (e.g. .rdf or .ttl).  Instead, they use content negotiation to serve an appropriate representation against
 * the URI of the graph, without redirection.
 * <p/>
 * This conforms to the common expectation that RDF documents and corresponding named graphs have the same URI.
 * <p/>
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:55:27 PM
 */
public class GraphResource extends Resource {
    private static final Logger LOGGER
            = TwitLogic.getLogger(GraphResource.class);

    protected final String selfURI;

    private String baseRef;
    private String typeSpecificId;
    protected Sail sail;
    private URI datasetURI;

    public GraphResource(final Context context,
                         final Request request,
                         final Response response) throws Exception {
        super(context, request, response);

        selfURI = request.getResourceRef().toString();

        /*
        System.out.println("selfURI = " + selfURI);
        System.out.println("baseRef = " + request.getResourceRef().getBaseRef());
        System.out.println("host domain = " + request.getResourceRef().getHostDomain());
        System.out.println("host identifier = " + request.getResourceRef().getHostIdentifier());
        System.out.println("hierarchical part = " + request.getResourceRef().getHierarchicalPart());
        System.out.println("host ref = " + request.getHostRef().toString());
        //*/

        getVariants().addAll(RDFStuff.getRDFVariants());

        baseRef = request.getResourceRef().getBaseRef().toString();
        typeSpecificId = selfURI.substring(baseRef.length());
        datasetURI = TwitLogicServer.getServer(context).getDatasetURI();
        sail = TwitLogicServer.getServer(context).getSail(request);
    }

    public boolean allowDelete() {
        return false;
    }

    public boolean allowGet() {
        return true;
    }

    public boolean allowPost() {
        return false;
    }

    public boolean allowPut() {
        return false;
    }

    @Override
    public Representation represent(final Variant variant) {
        return representInformationResource(variant);
    }

    private Representation representInformationResource(final Variant variant) {
        MediaType type = variant.getMediaType();
        RDFFormat format = RDFStuff.findRdfFormat(type);

        try {
            URI subject = sail.getValueFactory().createURI(selfURI);
            return getRDFRepresentation(subject, format);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private void addStatementsIn(final org.openrdf.model.Resource graph,
                                 final Collection<Statement> statements,
                                 final SailConnection c) throws SailException {
        CloseableIteration<? extends Statement, SailException> stIter
                = c.getStatements(null, null, null, false, graph);
        try {
            while (stIter.hasNext()) {
                statements.add(stIter.next());
            }
        } finally {
            stIter.close();
        }
    }

    private String resourceDescriptor() {
        for (TwitLogic.ResourceType t : TwitLogic.ResourceType.values()) {
            if (baseRef.contains(t.getUriPath())) {
                return t.getName();
            }
        }

        return "resource";
    }

    private void addDocumentMetadata(final Collection<Statement> statements,
                                     final ValueFactory vf) throws SailException {
        // Metadata about the document itself
        URI docURI = vf.createURI(selfURI);
        statements.add(vf.createStatement(docURI, RDF.TYPE, vf.createURI(FOAF.DOCUMENT)));
        statements.add(vf.createStatement(docURI, RDFS.LABEL, vf.createLiteral(""
                + resourceDescriptor() + " '" + typeSpecificId + "'")));
        // Note: we go to the trouble of special-casing the dataset URI, so that
        // it is properly rewritten, along with all other TwitLogic resource
        // URIs (which are rewritten through the Sail).
        statements.add(vf.createStatement(docURI, RDFS.SEEALSO, datasetURI));
    }

    private Representation getRDFRepresentation(final URI graph,
                                                final RDFFormat format) {
        try {
            Collection<Namespace> namespaces = new LinkedList<Namespace>();
            Collection<Statement> statements = new LinkedList<Statement>();

            SailConnection c = sail.getConnection();
            try {
                // Add virtual statements about the document.
                addDocumentMetadata(statements, sail.getValueFactory());

                // Add statements in this graph, preserving the graph component of the statements.
                addStatementsIn(graph, statements, c);

                // Select namespaces, for human-friendliness
                CloseableIteration<? extends Namespace, SailException> nsIter
                        = c.getNamespaces();
                try {
                    while (nsIter.hasNext()) {
                        namespaces.add(nsIter.next());
                    }
                } finally {
                    nsIter.close();
                }
            } finally {
                c.close();
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