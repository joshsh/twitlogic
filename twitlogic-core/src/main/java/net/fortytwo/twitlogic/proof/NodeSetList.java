package net.fortytwo.twitlogic.proof;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.vocabs.PMLJustification;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 4:12:56 AM
 */
public class NodeSetList extends PMLConstruct {
    private final NodeSet first;
    private final NodeSetList rest;

    public NodeSetList(final NodeSet first,
                       final NodeSetList rest,
                       final RDFizerContext context) {
        super(context);
        this.first = first;
        this.rest = rest;
    }

    protected void handleStatements(final Handler<Statement> handler) throws HandlerException {
        Resource g = context.getNamedGraph();
        ValueFactory vf = context.getValueFactory();

        handler.handle(vf.createStatement(
                self,
                RDF.TYPE,
                vf.createURI(PMLJustification.NODESETLIST),
                g));

        handler.handle(vf.createStatement(self, RDF.FIRST, first.self, g));
        first.handleStatements(handler);

        if (null == rest) {
            handler.handle(vf.createStatement(self, RDF.REST, RDF.NIL, g));
        } else {
            handler.handle(vf.createStatement(self, RDF.REST, rest.self, g));
            rest.handleStatements(handler);
        }
    }
}
