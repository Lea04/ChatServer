package org.oettel.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.businesslogic.UnicastServerService;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.model.message.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MessageListener implements Runnable {
    private ServerSocket serverSocket;
    private boolean running;
    private ObjectMapper mapper;
    private Message message;
//    private ElectionMessage electionMessage;
    private UnicastServerService unicastServerService;

    public MessageListener(UnicastServerService unicastServerService) throws IOException {
        this.unicastServerService = unicastServerService;
        this.serverSocket = new ServerSocket(ServerConfigurationSingleton.getInstance().getServerPort());
        this.mapper = new ObjectMapper();
    }


    @Override
    public void run() {
        System.out.println("Messagetlistener started...");
        running = true;


        while (running) {
            try {
                Socket socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream();
                Scanner inputScanner = new Scanner(inputStream);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String responseFromScanner = inputScanner.nextLine();

                message = mapper.readValue(responseFromScanner, Message.class);

                //System.out.println("Server Received Unicast from: " + socket.getInetAddress() + " :: Mesage Type: " + message.getMessageType() + " :: Mesage conntnet: " + message.getContent() );
                System.out.println("Received Unicast from: " + socket.getInetAddress() + " :: Mesage conntnet: " + message.getContent() );


                switch (message.getMessageType()) {
                    case SERVER_MESSAGE:
                        evaluateServerMessages(message, socket.getInetAddress());
                        break;
                    case CLIENT_MESSAGE:
                        evaluateClientMessages(message, socket.getInetAddress());
                        break;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("MessageListener stopped...");
    }

    private void evaluateServerMessages(Message message, InetAddress inetAddress) throws IOException {
        ServerMessage serverMessage = (ServerMessage) message;
        switch (serverMessage.getServerMessageType()) {
            case BROADCAST_RESPONSE:
                unicastServerService.handleBroadcastResponseMessage(inetAddress);
                break;
            case HEARTBEAT:
                unicastServerService.respondToHeartbeat(inetAddress);
                break;
            case HEARTBEAT_RESPONSE:
                unicastServerService.handleHeartBeatResponse(inetAddress);
                break;
            case ELECTION_MESSAGE:
                unicastServerService.handleElectionMessage((ElectionMessage) message);
                break;
            case LEADER_MESSAGE:
                unicastServerService.handleLeaderMessage((ElectionMessage) message);
                break;
            default:
                break;
        }
    }

    private void evaluateClientMessages(Message message, InetAddress inetAddress) throws IOException {
        ClientMessage clientMessage = (ClientMessage) message;
        switch (clientMessage.getClientMessageType()) {
            case CHAT_MESSAGE:
                unicastServerService.handleChatMessage(clientMessage);
                break;
            case NACK:
                unicastServerService.handleNack(clientMessage, inetAddress);
                break;
            default:
                break;
        }
    }


}

