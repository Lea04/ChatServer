package org.oettel.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.businesslogic.MulticastServerService;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.model.message.ElectionMessage;
import org.oettel.model.message.Message;
import org.oettel.model.message.ServerMessage;

import java.io.IOException;
import java.net.*;

import static org.oettel.configuration.Constants.MULTICAST_PORT;
import static org.oettel.configuration.Constants.MULTICAST_ADDRESS;

public class MulticastListener implements Runnable{

    private MulticastSocket multicastSocket;
    private boolean running;
    private byte[] buf = new byte[256];
    private InetAddress addressGroup;
    byte[] receivedJson =new byte[256];
    ObjectMapper mapper = new ObjectMapper();
    private MulticastServerService multicastServerService;




    public MulticastListener() throws IOException {
        this.addressGroup = InetAddress.getByName(MULTICAST_ADDRESS);
        this.multicastSocket = new MulticastSocket(MULTICAST_PORT);
        this.multicastSocket.joinGroup(new InetSocketAddress(addressGroup, ServerConfigurationSingleton.getInstance().getServerPort()), NetworkInterface.getByInetAddress(addressGroup));
        this.multicastServerService = new MulticastServerService();
    }


    @Override
    public void run() {
        System.out.println("Multicastlistener started...");
        running = true;

        while (running) {
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);


            try {
                multicastSocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            receivedJson = packet.getData();

            Message message = null;
            try {
                message = mapper.readValue(receivedJson, Message.class);
            } catch (IOException e) {
                e.printStackTrace();
            }


            System.out.println("Server Received Multicast from: " + packet.getAddress() + " :: Mesage Type: " + message.getMessageType() + " :: Mesage conntnet: " + message.getContent() );


            switch (message.getMessageType()) {
                case SERVER_MESSAGE:
                    evaluateServerMessages(message, packet);
                    break;
                case CLIENT_MESSAGE:
                    break;
                default:
                    //System.out.println("do default");
                    break;

            }
        }
        multicastSocket.close();
        System.out.println("Multicastlistener stopped...");
    }

    private void evaluateServerMessages(Message message, DatagramPacket packet) {
        ServerMessage serverMessage = (ServerMessage) message;
        switch (serverMessage.getServerMessageType()) {
            case ELECTION_INITIATING_MESSAGE:
                multicastServerService.electionInitiating();
                break;
            case LEADER_ANNOUNCEMENT:
                multicastServerService.handleLeaderAnnouncement(packet.getAddress());
                break;
        }
    }
}


