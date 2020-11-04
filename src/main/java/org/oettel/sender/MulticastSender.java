package org.oettel.sender;

import org.oettel.configuration.ServerConfigurationSingleton;

import java.io.IOException;
import java.net.*;

import static org.oettel.configuration.Constants.MULTICAST_PORT;
import static org.oettel.configuration.Constants.MULTICAST_ADDRESS;

public class MulticastSender {
    private MulticastSocket multicastSocket;
    private InetAddress addressGroup;
    private byte[] buf = new byte[512];

    public MulticastSender(int port) throws IOException {
        this.addressGroup = InetAddress.getByName(MULTICAST_ADDRESS);
        this.multicastSocket = new MulticastSocket(port);
        this.multicastSocket.joinGroup(new InetSocketAddress(addressGroup, ServerConfigurationSingleton.getInstance().getServerPort()), NetworkInterface.getByInetAddress(addressGroup));
    }



    public void sendMulticast(String msg, int port) throws IOException {
        System.out.println("MulticastMessage: " + msg);
        buf = msg.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, addressGroup, port);
        multicastSocket.send(packet);
        multicastSocket.close();

    }

    public void close() throws IOException {
        multicastSocket.close();
    }

}
