package net.fortytwo.twitlogic.xmpp;

import org.jivesoftware.smack.packet.Packet;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFHandlerException;

import java.util.Collection;
import java.io.Writer;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 15, 2009
 * Time: 10:31:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class RDFPacket extends Packet {
    private final Collection<Statement> statements;
    private final RDFFormat format;

    public RDFPacket(final Collection<Statement> statements,
                     final RDFFormat format) {
        this.statements = statements;
        this.format = format;
    }

    public String toXML() {
        StringWriter w = new StringWriter();

        try {
            writeRDF(w);
        } catch (RDFHandlerException e) {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return w.toString();
    }

    private void writeRDF(final Writer w) throws RDFHandlerException {
        RDFWriter writer = Rio.createWriter(format, w);
        writer.startRDF();
        try {
            for (Statement st : statements) {
                writer.handleStatement(st);
            }
        } finally {
            writer.endRDF();
        }
    }
}
