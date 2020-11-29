package org.oettel.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.configuration.HeartbeatListSingleton;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.model.Heartbeat;
import org.oettel.model.message.Message;
import org.oettel.model.message.ServerMessage;
import org.oettel.model.message.ServerMessageType;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MessageSender implements Runnable{
    private Socket socket;
    private ObjectMapper mapper;

    public MessageSender() {
    }

    public MessageSender(final InetAddress address) throws IOException {
        this.socket = new Socket(address, ServerConfigurationSingleton.getInstance().getServerPort());
        this.mapper = new ObjectMapper();
    }

    public void sendMessage(String message) throws IOException {
        //System.out.println("MessageSender: " + message);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println(message);
        //System.out.println("Message Send!!!");
    }

    public void close() throws IOException {
        socket.close();
    }


    @Override
    public void run() {
        System.out.println("#### Send initial heartbeat ####");
        ServerConfigurationSingleton.getInstance().getReplicaServer().forEach(inetAddress -> {
            Heartbeat heartbeat = new Heartbeat(inetAddress, false);
            HeartbeatListSingleton.getInstance().addReplicaToHeartbeatList(heartbeat);
            try {
                ObjectMapper mapper = new ObjectMapper();
                Message heartbeatMessage = new ServerMessage(ServerMessageType.HEARTBEAT, "heartbeat");
                String receivedJson = mapper.writeValueAsString(heartbeatMessage);
                this.rebuildSocket(inetAddress);
                this.sendMessage(receivedJson);
                this.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println("#### Initial heartbeat was send####\n");
    }

    private void rebuildSocket(InetAddress inetAddress) throws IOException {
        this.socket = new Socket(inetAddress, ServerConfigurationSingleton.getInstance().getServerPort());
    }
}

