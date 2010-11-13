package org.jivesoftware.smackx.packet;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.xmpp.RDFDocument;
import net.fortytwo.twitlogic.xmpp.RDFXMPPStuff;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.ByteArrayOutputStream;

/**
 * FIXME: it's very strange to have to put this class in a jivesoftware
 * package, but given the package-level access of the members that need to be
 * overridden, this seems to be the only way to go.
 * <p/>
 * User: josh
 * Date: Sep 23, 2009
 * Time: 7:25:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class RDFDocumentPEPItem extends PEPItem {
    private static final String TAGNAME = "content";
    private static final String NAMESPACE = TwitLogic.NAMESPACE;

    private final RDFDocument document;
    private final RDFFormat format;

    public RDFDocumentPEPItem(final String id,
                              final RDFDocument document,
                              final RDFFormat format) {
        super(id);
        this.document = document;
        this.format = format;
    }

    String getNode() {
        return RDFXMPPStuff.NODE;
    }

    String getItemDetailsXML() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        RDFWriter w = Rio.createWriter(format, bos);

        document.writeTo(w);

        String s = new String(bos.toByteArray());

        if (RDFFormat.RDFXML == format || RDFFormat.TRIX == format) {
            // Chop off the XML declaration.
            int i = s.indexOf('\n');
            s = s.substring(i + 1);
        }

        s = s.replaceAll("\n", "");

        s = "<" + TAGNAME + " xmlns='" + NAMESPACE + "'>" + s + "</" + TAGNAME + ">";
        //s = s.replaceAll("<rdf:", "<");
        //s = s.replaceAll("</rdf:", "</");
        //System.out.println("s = " + s);

        return s;
        //return "<body><one>foo</two></body>";
        //return "<content><one/></content>";
        //return "<body></body>";
    }
}
