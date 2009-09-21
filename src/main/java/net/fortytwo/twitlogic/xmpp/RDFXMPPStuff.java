package net.fortytwo.twitlogic.xmpp;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import java.io.IOException;
import java.util.Properties;

import net.fortytwo.twitlogic.TwitLogic;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 15, 2009
 * Time: 8:11:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class RDFXMPPStuff {

    private static final String RESOURCE = "SomeResource/";

    private final String server;
    private final int port;
    private final String reporterUsername;
    private final String reporterPassword;
    private final String reasonerUsername;
    private final String reasonerPassword;

    public RDFXMPPStuff() {
        Properties props = TwitLogic.getConfiguration();
        server = props.getProperty(TwitLogic.XMPP_SERVER);
        port = Integer.valueOf(props.getProperty(TwitLogic.XMPP_PORT));
        reporterUsername = props.getProperty(TwitLogic.XMPP_REPORTER_USERNAME);
        reporterPassword = props.getProperty(TwitLogic.XMPP_REPORTER_PASSWORD);
        reasonerUsername = props.getProperty(TwitLogic.XMPP_REASONER_USERNAME);
        reasonerPassword = props.getProperty(TwitLogic.XMPP_REASONER_PASSWORD);
    }

    public static void main(final String[] args) throws Exception {
        try {
            new RDFXMPPStuff().rdfPlay();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.currentThread().sleep(100000);
        System.exit(0);

        /*
        ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);
        config.setCompressionEnabled(true);
        config.setSASLAuthenticationEnabled(true);

        XMPPConnection c = new XMPPConnection(config);
        c.connect();
        c.login(REPORTER_USERNAME, REPORTER_PASSWORD, RESOURCE);

        // Create a new presence. Pass in false to indicate we're unavailable.
        Presence presence = new Presence(Presence.Type.unavailable);
        presence.setStatus("Gone fishing");
        // Send the packet (assume we have a XMPPConnection instance called "con").
        c.sendPacket(presence);

        c.disconnect();
        */
    }

    private XMPPConnection createConnection(final String userName,
                                            final String password) throws XMPPException {
        // Create a c to the jabber.org server on a specific port.
        ConnectionConfiguration config = new ConnectionConfiguration(server, port);

        XMPPConnection c = new XMPPConnection(config);
        c.connect();

        c.login(userName, password);

        Presence p = new Presence(Presence.Type.available);
        p.setPriority(127);
        c.sendPacket(p);

        return c;
    }

    private void registerRDFListener(final XMPPConnection c) {
        // Create a packet filter to listen for new messages from a particular
        // user. We use an AndFilter to combine two other filters.
        PacketFilter filter = new PacketTypeFilter(Message.class);
        // Assume we've created an XMPPConnection name "connection".

        // First, register a packet collector using the filter we created.
        PacketCollector myCollector = c.createPacketCollector(filter);
        // Normally, you'd do something with the collector, like wait for new packets.

        // Register the listener.
        c.addPacketListener(new RDFPacketListener(null), filter);
    }

    private void rdfPlay() throws XMPPException, RDFHandlerException, RepositoryException, IOException, RDFParseException {
        XMPPConnection senderConn = createConnection(reporterUsername, reporterPassword);
        XMPPConnection receiverConn = createConnection(reasonerUsername, reasonerPassword);

        Chat senderChat = senderConn.getChatManager().createChat(reasonerUsername, new MyListener());
        registerRDFListener(receiverConn);
        //Chat receiverChat = receiverConn.getChatManager().createChat(REPORTER_USERNAME, new MyListener());

        Message m = new Message();
        Repository senderRepo = createSampleRepository();
        PacketExtension ext = new RDFPacketExtension(RDFFormat.RDFXML, senderRepo);
        //PacketExtension ext = new RDFPacketExtension(RDFFormat.TRIX, senderRepo);
        m.addExtension(ext);
        m.setBody("testing");
        //m.setBody(ext.toXML());
        senderRepo.shutDown();
        m.setLanguage("en");
        m.setSubject("test message");
        //m.setBody("TwitLogic streaming RDF packet");
        m.setType(Message.Type.normal);
        System.out.println("message: " + m.toXML());
        senderChat.sendMessage(m);
    }

    /*
    private void rdfPlay() throws XMPPException, RDFHandlerException, RepositoryException {
        // Create a c to the jabber.org server on a specific port.
        ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);

        XMPPConnection c = new XMPPConnection(config);
        c.connect();

        c.login(REPORTER_USERNAME, REPORTER_PASSWORD);

        Presence p = new Presence(Presence.Type.available);
        c.sendPacket(p);

        Repository repo = createSampleRepository();

        MessageListener l = new MyListener();
        Chat ch = c.getChatManager().createChat(REASONER_USERNAME, l);
        Message m = new Message();
        m.setBody("Perfunctory message body.");
        m.addExtension(new RDFXMLPacketExtension(repo));
        m.setLanguage("en");
        m.setSubject("let's send some more *real* RDF/XML *with statements*, and chop off the XML declaration");
        m.setType(Message.Type.normal);
        System.out.println("message: " + m.toXML());
        ch.sendMessage(m);
    }*/

    private Repository createSampleRepository() throws RepositoryException, IOException, RDFParseException {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();

        RepositoryConnection rc = repo.getConnection();
        try {
            String baseURI = "http://example.org/bogus#";
            //rc.add(TwitLogic.class.getResourceAsStream("example-message.trig"), baseURI, RDFFormat.TRIG);
            rc.add(RDF.TYPE, RDF.TYPE, RDF.PROPERTY);
        } finally {
            rc.commit();
            rc.close();
        }

        return repo;
    }

    private class MyListener implements MessageListener {

        public void processMessage(final Chat chat,
                                   final Message message) {
            System.out.println("received message from chat " + chat + ": " + message);
        }
    }

    private class MyConnectionListener implements ConnectionListener {

        public void connectionClosed() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void connectionClosedOnError(Exception e) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void reconnectingIn(int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void reconnectionSuccessful() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void reconnectionFailed(Exception e) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
