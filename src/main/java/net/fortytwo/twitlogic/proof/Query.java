package net.fortytwo.twitlogic.proof;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.vocabs.PMLJustification;
import net.fortytwo.twitlogic.vocabs.PMLProvenance;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;

import java.util.Collection;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 3:09:08 AM
 */
public class Query extends PMLConstruct {
    private final Information content;
    private final Collection<NodeSet> answers;

    public Query(final Information content,
                 final Collection<NodeSet> answers,
                 final RDFizerContext context) {
        super(context);
        this.content = content;
        this.answers = answers;
    }

    protected void handleStatements(final Handler<Statement, RDFizerException> handler) throws RDFizerException {
        Resource g = context.getNamedGraph();
        ValueFactory vf = context.getValueFactory();

        handler.handle(vf.createStatement(
                self,
                RDF.TYPE,
                vf.createURI(PMLJustification.QUERY),
                g));

        handler.handle(vf.createStatement(
                self,
                vf.createURI(PMLProvenance.HASCONTENT),
                content.self,
                g));
        content.handleStatements(handler);

        for (NodeSet answer : answers) {
            handler.handle(vf.createStatement(
                    self,
                    vf.createURI(PMLJustification.HASANSWER),
                    answer.self,
                    g));
            answer.handleStatements(handler);
        }
    }
}
