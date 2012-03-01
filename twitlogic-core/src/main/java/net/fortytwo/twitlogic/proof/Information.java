package net.fortytwo.twitlogic.proof;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.vocabs.PMLProvenance;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Information extends PMLConstruct {
    private final String rawString;

    public Information(final String rawString,
                       final RDFizerContext context) {
        super(context);
        this.rawString = rawString;
    }

    protected void handleStatements(final Handler<Statement> handler) throws HandlerException {
        Resource g = context.getNamedGraph();
        ValueFactory vf = context.getValueFactory();

        handler.handle(vf.createStatement(
                self,
                RDF.TYPE,
                vf.createURI(PMLProvenance.INFORMATION),
                g));

        handler.handle(vf.createStatement(
                self,
                vf.createURI(PMLProvenance.HASRAWSTRING),
                vf.createLiteral(rawString),
                g));
    }
}
