package org.oettel.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.configuration.HeartbeatListSingleton;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.model.Heartbeat;
import org.oettel.model.message.Message;
import org.oettel.model.message.ServerMessage;
import org.oettel.model.message.ServerMessageType;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class BroadcastSender{

    private DatagramSocket socket;
    private InetAddress address;
    private byte[] buferReader = new byte[256];

    private byte[] buf;

    public BroadcastSender() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);
    }

    public void sendEcho(String msg) throws IOException {
        buf = msg.getBytes();
        getBroadcastList().forEach(inetAddress -> {
            try {
                sendBroadcast(inetAddress);
            } catch (IOException e) {
                //System.out.println("Broadcast error");
                e.printStackTrace();
            }
        });
    }

    private void sendBroadcast(final InetAddress inetAddress) throws IOException {
        //System.out.println("Send Broadcast to: " + inetAddress.toString());
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, inetAddress,
                ServerConfigurationSingleton.getInstance().getServerPort());
        socket.send(packet);
        close();

    }

    private List<InetAddress> getBroadcastList() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces
                = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }

    public void close() {
        socket.close();
    }


}

