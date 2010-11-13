package net.fortytwo.twitlogic.xmpp;

import net.fortytwo.twitlogic.flow.Handler;
import org.jivesoftware.smackx.PEPManager;
import org.jivesoftware.smackx.packet.PEPItem;
import org.jivesoftware.smackx.packet.RDFDocumentPEPItem;
import org.openrdf.rio.RDFFormat;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 23, 2009
 * Time: 6:56:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class RDFPubSubSender implements Handler<RDFDocument, Exception> {
    private final Handler<RDFDocument, Exception> handler;
    private final PEPManager manager;
    private final RDFFormat format;

    public RDFPubSubSender(final PEPManager manager,
                           final RDFFormat format) {
        this.manager = manager;
        this.format = format;

        handler = new Handler<RDFDocument, Exception>() {
            public boolean handle(final RDFDocument document) throws Exception {
                return publish(document);
            }
        };
    }

    private boolean publish(final RDFDocument document) {
        PEPItem item = new RDFDocumentPEPItem(RDFXMPPStuff.randomId(), document, format);
        manager.publish(item);

        return true;
    }

    public boolean handle(final RDFDocument document) throws Exception {
        return handler.handle(document);
    }
}
