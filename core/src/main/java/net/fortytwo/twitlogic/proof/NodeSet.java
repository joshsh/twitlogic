package net.fortytwo.twitlogic.proof;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.vocabs.PMLJustification;
import net.fortytwo.twitlogic.vocabs.PMLTrust;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 3:02:12 AM
 */
public class NodeSet extends PMLConstruct {
    private final Resource conclusion;
    private final Float floatValue;
    private final InferenceStep consequentOf;

    public NodeSet(final Resource conclusion,
                   final Float floatValue,
                   final InferenceStep consequentOf,
                   final RDFizerContext context) {
        super(context);
        this.conclusion = conclusion;
        this.floatValue = floatValue;
        this.consequentOf = consequentOf;
    }

    protected void handleStatements(final Handler<Statement> handler) throws HandlerException {
        Resource g = context.getNamedGraph();
        ValueFactory vf = context.getValueFactory();

        handler.handle(vf.createStatement(
                self,
                RDF.TYPE,
                vf.createURI(PMLJustification.NODESET),
                g));

        handler.handle(vf.createStatement(self,
                vf.createURI(PMLJustification.HASCONCLUSION),
                conclusion,
                g));

        if (null != floatValue) {
            handler.handle(vf.createStatement(
                    self,
                    vf.createURI(PMLTrust.HASFLOATVALUE),
                    vf.createLiteral(floatValue),
                    g));
        }

        handler.handle(vf.createStatement(self,
                vf.createURI(PMLJustification.ISCONSEQUENTOF),
                consequentOf.self,
                g));
        consequentOf.handleStatements(handler);
    }
}
