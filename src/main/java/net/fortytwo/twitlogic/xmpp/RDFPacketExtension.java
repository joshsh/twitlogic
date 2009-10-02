package net.fortytwo.twitlogic.xmpp;

import net.fortytwo.twitlogic.TwitLogic;
import org.jivesoftware.smack.packet.PacketExtension;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.ByteArrayOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 20, 2009
 * Time: 8:53:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class RDFPacketExtension implements PacketExtension {
    private static final String TAGNAME = "content";
    private static final String NAMESPACE = TwitLogic.NAMESPACE;

    private final RDFFormat format;
    private final String serialized;

    /**
     * Creates an extension by serializing all statements in the given Repository.
     *
     * @param id
     * @param format
     * @param repo   the repository to serialize
     * @throws org.openrdf.rio.RDFHandlerException
     *
     * @throws org.openrdf.repository.RepositoryException
     *
     */
    public RDFPacketExtension(final String id,
                              final RDFFormat format,
                              final Repository repo) throws RDFHandlerException, RepositoryException {
        this.format = format;
        serialized = serialize(repo);
    }

    public String getElementName() {
        return TAGNAME;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    public String toXML() {
        return serialized;
    }

    // Note: this assumes a particular style of serialization by Rio which is not strictly defined.
    //       Specifically, it assumes that the first line contains an XML declaration (which is discarded),
    //       and that there are no comments or entity declarations (which are not allowed in an XMPP stream).

    private String serialize(final Repository repo) throws RDFHandlerException, RepositoryException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        RDFWriter w = Rio.createWriter(format, bos);

        RepositoryConnection rc = repo.getConnection();
        try {
            rc.export(w);
        } finally {
            rc.close();
        }

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
