package net.fortytwo.twitlogic.server;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.twitlogic.TwitLogic;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
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
 * Information and non-information resources are distinguished by the suffix of the resource's URI:
 * 1) information resource URIs end in .rdf or .trig
 * 2) non-information resources have no such suffix (and TwitLogic will not make statements about such URIs)
 * <p/>
 * A request for an information resource is fulfilled with the resource itself.  No content negotiation occurs.
 * <p/>
 * A request for a non-information resource is fulfilled with a 303-redirect to an information resource of the appropriate media type.
 * <p/>
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:55:27 PM
 */
public class WebResource extends Resource {
    private static final Logger LOGGER
            = TwitLogic.getLogger(WebResource.class);

    enum ResourceType {
        InformationResource, NonInformationResource
    }

    protected ResourceType resourceType;
    protected String selfURI;
    protected String subjectResourceURI;
    protected Sail sail;
    private RDFFormat format = null;

    public WebResource(final Context context,
                       final Request request,
                       final Response response) throws Exception {
        super(context, request, response);

        selfURI = request.getResourceRef().toString();
        //System.out.println("selfURI = " + selfURI);

        int i = selfURI.lastIndexOf(".");
        if (i > 0) {
            String suffix = selfURI.substring(i + 1);
            subjectResourceURI = selfURI.substring(0, i);
            format = RDFStuff.findFormat(suffix);
        }

        if (null == format) {
            resourceType = ResourceType.NonInformationResource;
            getVariants().addAll(RDFStuff.getRDFVariants());
        } else {
            resourceType = ResourceType.InformationResource;
            getVariants().add(RDFStuff.findVariant(format));
            sail = TwitLogicServer.getWiki(context).getSail(request);
        }
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
        switch (resourceType) {
            case InformationResource:
                return representInformationResource();
            case NonInformationResource:
                return representNonInformationResource(variant);
            default:
                throw new IllegalStateException("no such resource type: " + resourceType);
        }
    }

    private Representation representInformationResource() {
        try {
            URI subject = sail.getValueFactory().createURI(subjectResourceURI);
            return getRDFRepresentation(subject, format);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private Representation representNonInformationResource(final Variant variant) {
        MediaType type = variant.getMediaType();
        RDFFormat format = RDFStuff.findRdfFormat(type);
        String suffix = RDFStuff.findSuffix(format);

        getResponse().redirectSeeOther(selfURI + "." + suffix);

        return null;
        //return new StringRepresentation("see the indicated URI for an associated RDF description of this resource");
    }

    private void addIncidentStatements(final org.openrdf.model.Resource vertex,
                                       final Collection<Statement> statements,
                                       final SailConnection c) throws SailException {
        // Select outbound statements
        CloseableIteration<? extends Statement, SailException> stIter
                = c.getStatements(vertex, null, null, false);
        try {
            while (stIter.hasNext()) {
                statements.add(stIter.next());
            }
        } finally {
            stIter.close();
        }

        // Select inbound statements
        stIter = c.getStatements(null, null, vertex, false);
        try {
            while (stIter.hasNext()) {
                statements.add(stIter.next());
            }
        } finally {
            stIter.close();
        }
    }

    // Note: a SPARQL query might be more efficient (in applications other than TwitLogic)
    private void addGraphSeeAlsoStatements(final org.openrdf.model.Resource graph,
                                           final Collection<Statement> statements,
                                           final SailConnection c,
                                           final ValueFactory vf) throws SailException {
        Set<URI> describedResources = new HashSet<URI>();
        CloseableIteration<? extends Statement, SailException> iter
                = c.getStatements(null, null, null, false, graph);
        try {
            while (iter.hasNext()) {
                Statement st = iter.next();
                Value s = st.getSubject();
                Value o = st.getObject();

                if (s instanceof URI) {
                    describedResources.add((URI) s);
                }

                if (o instanceof URI) {
                    describedResources.add((URI) o);
                }
            }
        } finally {
            iter.close();
        }

        for (URI r : describedResources) {
            statements.add(vf.createStatement(graph, RDFS.SEEALSO, r, TwitLogic.AUTHORITATIVE_GRAPH));
        }
    }

    private Representation getRDFRepresentation(final URI subject,
                                                final RDFFormat format) {
        try {
            Collection<Namespace> namespaces = new LinkedList<Namespace>();
            Collection<Statement> statements = new LinkedList<Statement>();

            SailConnection sc = sail.getConnection();
            try {
                // Add statements incident on the resource itself.
                addIncidentStatements(subject, statements, sc);

                /*
                // Due to the nature of the TwitLogic data set, we also need
                // some key statements about the graphs the above statements
                // are in.
                Set<org.openrdf.model.Resource> graphs = new HashSet<org.openrdf.model.Resource>();
                for (Statement st : statements) {
                    org.openrdf.model.Resource graph = st.getContext();
                    if (null != graph) {
                        graphs.add(graph);
                    }
                }
                // Note: self will not be in this set, as graphs don't
                // describe themselves in TwitLogic.
                for (org.openrdf.model.Resource graph : graphs) {
                    addIncidentStatements(graph, statements, sc);
                }
                */

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