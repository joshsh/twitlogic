package net.fortytwo.twitlogic.util;

import net.fortytwo.sesametools.rdftransaction.RDFTransactionSail;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Note: typical size of a packet based on one of the test tweets is 6000 bytes.
 * <p/>
 * Note: "The limit on a UDP datagram payload is 65535-28=65507 bytes, and the practical limit is the MTU of the path
 * which is more like 1460 bytes if you're lucky."
 * <p/>
 * See:
 * http://stackoverflow.com/questions/3396813/message-too-long-for-udp-socket-after-setting-sendbuffersize
 * <p/>
 * User: josh
 * Date: Aug 23, 2010
 * Time: 12:23:24 PM
 */
public class UdpTransactionSail extends RDFTransactionSail {
    private final DatagramSocket socket;
    private final InetAddress address;
    private final int ports[];
    private int portIndex = 0;

    /**
     * @param baseSail base Sail
     * @param address  address of the remote host
     * @param ports    receiving ports of the remote host. This Sail will load-balance evenly across those ports.
     */
    public UdpTransactionSail(final Sail baseSail,
                              final InetAddress address,
                              final int... ports) throws SocketException, UnknownHostException {
        super(baseSail);

        this.socket = new DatagramSocket();
        this.address = address;
        this.ports = ports;
    }

    public void uploadTransactionEntity(byte[] bytes) throws SailException {
        //System.out.println(new String(bytes));
        /*
        try {
            System.out.println("message length: " + bytes.length + " (compressed: " + zipStringToBytes(bytes).length + ")");
        } catch (IOException e) {
            throw new SailException(e);
        }
        /*/
        //*
        try {
            socket.send(new DatagramPacket(bytes, bytes.length, address, ports[portIndex]));
            portIndex = (portIndex + 1) % ports.length;
        } catch (IOException e) {
            throw new SailException(e);
        }//*/
    }
}
