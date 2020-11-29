package org.oettel.businesslogic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.configuration.Constants;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.model.message.*;
import org.oettel.model.vectorclock.VectorClockEntry;
import org.oettel.model.vectorclock.VectorClockSingleton;
import org.oettel.sender.MulticastSender;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class MulticastServerService {

    /**
     * Initiates the leader election algorithmn
     */
    public void electionInitiating(){
        LeaderElectionService leaderElectionService = new LeaderElectionService();
        leaderElectionService.startElection();
    }

    /**
     * Sets the leader IP
     */
    public void handleLeaderAnnouncement(InetAddress inetAddress){
        ServerConfigurationSingleton.getInstance().setLeader(inetAddress);
    }

    public void respondToClientVectorMessage(InetAddress address) {
        System.out.println("Server sends CLIENT_VECTOR_RESPONSE to: " + address.toString());

        AtomicBoolean test = new AtomicBoolean(true);
        VectorClockSingleton.getInstance().getVectorClockEntryList().forEach(vectorClockEntry -> {
            if(vectorClockEntry.getIpAdress().equals(address)){
                test.set(false);
            }
            else{}
        });

        if(test.get()) {
            VectorClockSingleton
                    .getInstance()
                    .addVectorClockEntryToList(new VectorClockEntry(address, 0));
        }

        VectorClockSingleton.getInstance().getVectorClockEntryList().forEach(vectorClockEntry -> {
            System.out.print("Address: " + vectorClockEntry.getIpAdress().toString() + " ");
            System.out.println("Clockcount: " + vectorClockEntry.getClockCount());
        });

        try {
            MulticastSender multicastSender = new MulticastSender(Constants.CLIENT_MULTICAST_PORT);
            Message message = new ClientMessage(ClientMessageType.VECTOR_BROADCAST_RESPONSE, "VECTOR_BROADCAST_RESPONSE", VectorClockSingleton.getInstance().getVectorClockEntryList());
            ObjectMapper mapper = new ObjectMapper();
            String messageAsJson = mapper.writeValueAsString(message);
            multicastSender.sendMulticast(messageAsJson, Constants.CLIENT_MULTICAST_PORT);
            multicastSender.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void replicate(ServerMessage message) {
        if(!ServerConfigurationSingleton.getInstance().getIsLeader()){
            ServerConfigurationSingleton.getInstance().getMessageQueue().add(message);
        }

    }

    public void replicateVectorClock(ServerMessage message) {
        if(!ServerConfigurationSingleton.getInstance().getIsLeader()){
            VectorClockSingleton.getInstance().setVectorClockEntryList(message.getVectorClockEntryList());
        }

    }
}
