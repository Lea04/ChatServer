package org.oettel.businesslogic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.configuration.Constants;
import org.oettel.configuration.HeartbeatListSingleton;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.model.message.*;
import org.oettel.model.vectorclock.VectorClockEntry;
import org.oettel.model.vectorclock.VectorClockSingleton;
import org.oettel.sender.MessageSender;
import org.oettel.sender.MulticastSender;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UnicastServerService {
    ObjectMapper mapper = new ObjectMapper();

    public void handleBroadcastResponseMessage(InetAddress inetAddress) throws IOException {
        System.out.println("Server receied BroadcastResponse from: "+inetAddress);
        if (!ServerConfigurationSingleton.getInstance().getReplicaServer().contains(inetAddress)) {
            ServerConfigurationSingleton.getInstance().getReplicaServer().add(inetAddress);
            //TODO: State replication
        }



        //TODO: Initiate Leader election
       ObjectMapper objectMapper = new ObjectMapper();
        Message electionInitiatingMessage = new ServerMessage(ServerMessageType.ELECTION_INITIATING_MESSAGE, "election_initiating_message");
        MulticastSender multicastSender = new MulticastSender(Constants.MULTICAST_PORT);
        multicastSender.sendMulticast(objectMapper.writeValueAsString(electionInitiatingMessage), Constants.MULTICAST_PORT);

    }

    public void respondToHeartbeat(InetAddress inetAddress) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Message heartbeatResponseMessage = new ServerMessage(ServerMessageType.HEARTBEAT_RESPONSE, "heartbeat_response");
        String receivedJson = mapper.writeValueAsString(heartbeatResponseMessage);
        MessageSender messageSender = new MessageSender(inetAddress);
        messageSender.sendMessage(receivedJson);
    }

    /**
     * Sets Heartbeat list to true for each server which send a heartbeat_response
     * @param inetAddress
     */
    public void handleHeartBeatResponse(InetAddress inetAddress) {
        //System.out.println("Case Heartbeat_response********************************************************************************");
        HeartbeatListSingleton.getInstance().getHeartbeatList().forEach(heartbeat -> {
            if (heartbeat.getInetAddress().equals(inetAddress)) {
                heartbeat.setHeartbeatOK(true);
                //System.out.println("####Set heartbeat true");
            }
        });
    }

    /**
     * Handles a election message within the election algoritm
     *
     * @param message
     * @throws IOException
     */
    public void handleElectionMessage(ElectionMessage message) throws IOException {

        LeaderElectionService leaderElection = new LeaderElectionService();
        leaderElection.receiveElectionMessage(message.getMid(), message.isLeader());

    }

    public void handleLeaderMessage(ElectionMessage message) throws IOException {

        LeaderElectionService leaderElection = new LeaderElectionService();
        leaderElection.receiveElectionMessage(message.getMid(), message.isLeader());

    }

    public void handleChatMessage(ClientMessage message) throws IOException {
        MulticastSender multicastSenderClient = new MulticastSender(Constants.CLIENT_MULTICAST_PORT);
        VectorClockSingleton.getInstance().updateVectorClock();
        message.getVectorClockEntries().forEach(externalVectorClockEntry -> {
            VectorClockSingleton.getInstance().updateExternalValues(externalVectorClockEntry);
        });

        VectorClockSingleton.getInstance().updateVectorClock();
        VectorClockSingleton.getInstance().updateVectorClock();
        message.setVectorClockEntries(VectorClockSingleton.getInstance().getVectorClockEntryList());
        //Message messageForwarder = message;

        ObjectMapper objectMapper = new ObjectMapper();
        message.setMessageType(MessageType.CLIENT_MESSAGE);
        message.setQueueIdCounter(ServerConfigurationSingleton.getInstance().getQueueIdCounter());
        Message messageForwarder = message;
        multicastSenderClient.sendMulticast(objectMapper.writeValueAsString(messageForwarder), Constants.CLIENT_MULTICAST_PORT);

        VectorClockSingleton.getInstance().getVectorClockEntryList().forEach(vectorClockEntry -> {
            System.out.print("Address: " + vectorClockEntry.getIpAdress().toString() + " ");
            System.out.println("Clockcount: " + vectorClockEntry.getClockCount());
        });

        //Replication Message
/*
        ServerMessage repMessage = new ServerMessage();
        repMessage.setMessageType(MessageType.SERVER_MESSAGE);
        repMessage.setServerMessageType(ServerMessageType.REPLICATION_QUEUE);
        repMessage.setContent(message.getContent());
        repMessage.setVectorClockEntryList(message.getVectorClockEntries());

        ServerConfigurationSingleton.getInstance().addMessageToHoldbackQueue(repMessage);

        MulticastSender multicastSender = new MulticastSender(Constants.MULTICAST_PORT);
//        message.setClientMessageType(ClientMessageType.REPLICATION);
        multicastSender.sendMulticast(mapper.writeValueAsString(repMessage),Constants.MULTICAST_PORT);
        multicastSender.close();*/

        //Replication VectorClock
        if(ServerConfigurationSingleton.getInstance().getReplicaServer().size()>1) {
            MulticastSender multicastSender2 = new MulticastSender(Constants.MULTICAST_PORT);
            ServerMessage serverMessage = new ServerMessage(ServerMessageType.REPLICATION, "Replicate VectorClock", VectorClockSingleton.getInstance().getVectorClockEntryList());
            multicastSender2.sendMulticast(mapper.writeValueAsString(serverMessage), Constants.MULTICAST_PORT);
            multicastSender2.close();
        }


    }

    public void handleNack(ClientMessage message, InetAddress inetAddress) throws IOException {
        message.getQueueIdCounter();
        ServerConfigurationSingleton.getInstance().getHoldbackQueue().forEach(holdBackQueueEntry -> {
            if(holdBackQueueEntry.getQueueIdCounter() == message.getQueueIdCounter()){
            try {
                ServerMessage serverMessage = holdBackQueueEntry;


                ClientMessage clientMessage = new ClientMessage();
                clientMessage.setMessageType(MessageType.CLIENT_MESSAGE);
                clientMessage.setClientMessageType(ClientMessageType.NACK);
                clientMessage.setContent(message.getContent());
                clientMessage.setVectorClockEntries(message.getVectorClockEntries());

                String messageJson = mapper.writeValueAsString(clientMessage);

                MessageSender messageSender = new MessageSender(inetAddress);
                messageSender.sendMessage(messageJson);
                messageSender.close();
            }catch (Exception e) {
                    e.printStackTrace();
            }

            }

        });

    }

}
