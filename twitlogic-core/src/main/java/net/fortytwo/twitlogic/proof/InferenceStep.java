package net.fortytwo.twitlogic.proof;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.vocabs.PMLJustification;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class InferenceStep extends PMLConstruct {
    private final Resource inferenceRule;
    private final NodeSetList antecedentList;

    public InferenceStep(final Resource inferenceRule,
                         final NodeSetList antecedentList,
                         final RDFizerContext context) {
        super(context);
        this.inferenceRule = inferenceRule;
        this.antecedentList = antecedentList;
    }

    protected void handleStatements(final Handler<Statement> handler) throws HandlerException {
        Resource g = context.getNamedGraph();
        ValueFactory vf = context.getValueFactory();


        handler.handle(vf.createStatement(
                self,
                RDF.TYPE,
                vf.createURI(PMLJustification.INFERENCESTEP),
                g));

        handler.handle(vf.createStatement(
                self,
                vf.createURI(PMLJustification.HASINFERENCERULE),
                inferenceRule,
                g));

        if (null == antecedentList) {
            handler.handle(vf.createStatement(
                    self,
                    vf.createURI(PMLJustification.HASANTECEDENTLIST),
                    RDF.NIL,
                    g));
        } else {
            handler.handle(vf.createStatement(
                    self,
                    vf.createURI(PMLJustification.HASANTECEDENTLIST),
                    antecedentList.self,
                    g));
            antecedentList.handleStatements(handler);
        }
    }
}
