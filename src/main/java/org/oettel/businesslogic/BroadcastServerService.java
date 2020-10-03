package org.oettel.businesslogic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.model.message.*;
import org.oettel.model.vectorclock.VectorClockEntry;
import org.oettel.model.vectorclock.VectorClockSingleton;
import org.oettel.sender.MessageSender;

import java.io.IOException;
import java.net.InetAddress;

public class BroadcastServerService {

    /**
     *Sends a broadcast_response to the broadcast initiating server
     *
     * @param address
     */
    public void respondToBroadcastMessage(InetAddress address) {
        System.out.print("Server sends BROADCAST_RESPONSE to: " + address.toString() + " :: ");
        try {
            MessageSender messageSender = new MessageSender(address);
            Message message = new ServerMessage(ServerMessageType.BROADCAST_RESPONSE, "broadcast_response");
            ObjectMapper mapper = new ObjectMapper();
            String messageAsJson = mapper.writeValueAsString(message);
            messageSender.sendMessage(messageAsJson);
            messageSender.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        //Adds a server InetAddress to the ReclicaList and ensures that the ReplicaList contains the a InetAddress is contained at most once
        if(!ServerConfigurationSingleton.getInstance().getReplicaServer().contains(address)){
            ServerConfigurationSingleton.getInstance().addReplicaToReplicaList(address);
            //TODO: State replication
        }

    }

    public void respondToClientBroadcastMessage(InetAddress address) {
        System.out.println("Server sends CLIENT_BROADCAST_RESPONSE to: " + address.toString());
        try {
            MessageSender messageSender = new MessageSender(address);
            Message message = new ClientMessage(ClientMessageType.CLIENT_BROADCAST_RESPONSE, "CLIENT_BROADCAST_RESPONSE");
            ObjectMapper mapper = new ObjectMapper();
            String messageAsJson = mapper.writeValueAsString(message);
            messageSender.sendMessage(messageAsJson);
            messageSender.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void respondToHeartbeat(InetAddress inetAddress) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Message heartbeatResponseMessage = new ServerMessage(ServerMessageType.HEARTBEAT_RESPONSE, "heartbeat_response");
        String receivedJson = mapper.writeValueAsString(heartbeatResponseMessage);
        MessageSender messageSender = new MessageSender(inetAddress);
        messageSender.sendMessage(receivedJson);
    }


}
