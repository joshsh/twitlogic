package net.fortytwo.twitlogic.xmpp;

import net.fortytwo.twitlogic.flow.Handler;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.PEPListener;
import org.jivesoftware.smackx.PEPManager;
import org.jivesoftware.smackx.packet.PEPEvent;
import org.jivesoftware.smackx.packet.RDFDocumentPEPItem;
import org.jivesoftware.smackx.provider.PEPProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 23, 2009
 * Time: 6:55:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class RDFPubSubReceiver {
    private final Handler<RDFDocument> handler;

    public RDFPubSubReceiver(final Handler<RDFDocument> handler) {
        this.handler = handler;
    }

    public void registerWith(final PEPManager manager) {
        manager.addPEPListener(new PEPListener() {
            public void eventReceived(final String inFrom,
                                      final PEPEvent inEvent) {
                System.out.println("Event received from " + inFrom + ": " + inEvent);
            }
        });

        PEPProvider pepProvider = new PEPProvider();
        pepProvider.registerPEPParserExtension(RDFXMPPStuff.NODE, new RDFDocumentProvider());
        ProviderManager.getInstance().addExtensionProvider("event", "http://jabber.org/protocol/pubsub#event", pepProvider);
    }

    public class RDFDocumentProvider implements PacketExtensionProvider {
        public RDFDocumentProvider() {

        }

        // Note: the JavaDoc comments say the return type should be PEPItem, although the API demands only PacketExtension
        public RDFDocumentPEPItem parseExtension(final XmlPullParser parser) throws Exception {
            System.out.println("parsing the document, although I'd rather just pass it off to Sesame as text...");
            return null;
        }
    }

    /*
    private void exampleFromJavadocs(final XMPPConnection smackConnection) {
        PEPManager pepManager = new PEPManager(smackConnection);
        pepManager.addPEPListener(new PEPListener() {
            public void eventReceived(String inFrom, PEPEvent inEvent) {
                System.out.println("Event received: " + inEvent);
            }
        });
        PEPProvider pepProvider = new PEPProvider();
        pepProvider.registerPEPParserExtension("http://jabber.org/protocol/tune", new TuneProvider());
        ProviderManager.getInstance().addExtensionProvider("event", "http://jabber.org/protocol/pubsub#event", pepProvider);
        Tune tune = new Tune("jeff", "1", "CD", "My Title", "My Track");
        pepManager.publish(tune);

    }
    //*/
}
