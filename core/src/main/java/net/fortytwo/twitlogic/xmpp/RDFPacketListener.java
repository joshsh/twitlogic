package net.fortytwo.twitlogic.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.openrdf.rio.RDFHandler;

/**
 * Created by IntelliJ IDEA.
* User: josh
* Date: Sep 20, 2009
* Time: 8:50:23 PM
* To change this template use File | Settings | File Templates.
*/
class RDFPacketListener implements PacketListener {
    private final RDFHandler handler;

    public RDFPacketListener(final RDFHandler handler) {
        this.handler = handler;
    }

    public void processPacket(final Packet packet) {
        String from = packet.getFrom();
        String to = packet.getTo();
        String content = packet.toXML();

        System.out.println("Received a packet");
        System.out.println("    from: " + from);
        System.out.println("    to: " + to);
        System.out.println("    content: " + content);
    }
}
