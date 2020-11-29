package org.oettel.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.businesslogic.MulticastServerService;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.businesslogic.BroadcastServerService;
import org.oettel.model.message.ClientMessage;
import org.oettel.model.message.Message;
import org.oettel.model.message.ServerMessage;

import java.io.IOException;
import java.net.*;

public class BroadcastListener implements Runnable {
    private DatagramSocket socket;
    private boolean running;
    private BroadcastServerService broadcastServerService;
    private MulticastServerService multicastServerService;
    private byte[] buf = new byte[256];
    byte[] receivedJson =new byte[256];
    ObjectMapper mapper = new ObjectMapper();

    public BroadcastListener(BroadcastServerService broadcastServerService, MulticastServerService multicastServerService) throws SocketException {
        this.broadcastServerService = broadcastServerService;
        this.multicastServerService = multicastServerService;
        System.out.println("#### Initialization of Broadcast listener ####");
        socket = new DatagramSocket(ServerConfigurationSingleton.getInstance().getServerPort());
        System.out.println("#### Broadcast listener initialized ####\n");
    }

    @Override
    public void run() {
        System.out.println("Server BroadcastListener started...");
        running = true;

        while (running) {
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);

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

            //System.out.println("Server Received Broadcast from: " + packet.getAddress() + " :: Mesage Type: " + message.getMessageType() + " :: Mesage conntnet: " + message.getContent() );
            //System.out.println("Received Broadcast from: " + packet.getAddress() +":: Mesage conntnet: " + message.getContent() );



            switch (message.getMessageType()) {
                case SERVER_MESSAGE:
                    try {
                        evaluateServerMessages(message, packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case CLIENT_MESSAGE:
                    evaluateClientMessages(message, packet);
                    break;
                default:
                    System.out.println("do default");
                    break;

            }

        }
        socket.close();
        System.out.println("Server BroadcastListener stopped...");
    }

    private void evaluateServerMessages(Message message, DatagramPacket packet) throws IOException {
        ServerMessage serverMessage = (ServerMessage) message;
        switch (serverMessage.getServerMessageType()) {
            case BROADCAST:
                broadcastServerService.respondToBroadcastMessage(packet.getAddress());
                break;
            case HEARTBEAT:
                broadcastServerService.respondToHeartbeat(packet.getAddress());
                break;
        }
    }

    private void evaluateClientMessages(Message message, DatagramPacket packet) {
        ClientMessage clientMessage = (ClientMessage) message;
        switch (clientMessage.getClientMessageType()) {
            case CLIENT_BROADCAST:
                 if(ServerConfigurationSingleton.getInstance().getIsLeader()) {
                    broadcastServerService.respondToClientBroadcastMessage(packet.getAddress());
                    multicastServerService.respondToClientVectorMessage(packet.getAddress());
                }
                break;
        }
    }




}

