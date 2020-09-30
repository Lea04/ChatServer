

package org.oettel.businesslogic;

import org.oettel.configuration.Constants;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.model.message.*;
import org.oettel.sender.MessageSender;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.sender.MulticastSender;


public class LeaderElectionService implements Runnable {

    private InetAddress mid;
    private InetAddress pid;


    public LeaderElectionService(){};
    public LeaderElectionService(InetAddress mid) {
        this.pid = ServerConfigurationSingleton.getInstance().getServerAddress();
        this.mid = mid;
    }

    private void formRing()  {
        Collections.sort(ServerConfigurationSingleton.getInstance().getReplicaServer(),new InetAddressComparator());
    }

    private InetAddress get_neighbour()  {
        int current_member_index = ServerConfigurationSingleton.getInstance().getReplicaServer().indexOf(ServerConfigurationSingleton.getInstance().getServerAddress());
        int next_member_index =current_member_index+1;

        if((next_member_index) == ServerConfigurationSingleton.getInstance().getReplicaServer().size()){
            return ServerConfigurationSingleton.getInstance().getReplicaServer().get(0);
        }
        else {
            return ServerConfigurationSingleton.getInstance().getReplicaServer().get(next_member_index);
        }
    }

    private void sendElectionMessage(InetAddress inetAddress, boolean isLeader) throws IOException {
        //System.out.println("Election Message send to" + inetAddress);
        MessageSender messageSender = new MessageSender(get_neighbour());
        ObjectMapper mapper = new ObjectMapper();
        Message electionMessage = new ElectionMessage(ServerMessageType.ELECTION_MESSAGE,"election_message", inetAddress, isLeader);
        messageSender.sendMessage(mapper.writeValueAsString(electionMessage));
    }

    private void sendLeaderMessage(InetAddress inetAddress, boolean isLeader) throws IOException {
        //System.out.println("Leader Message send to" + inetAddress);
        MessageSender messageSender = new MessageSender(get_neighbour());
        ObjectMapper mapper = new ObjectMapper();
        Message electionMessage = new ElectionMessage(ServerMessageType.LEADER_MESSAGE,"leader_message", inetAddress, isLeader);
        messageSender.sendMessage(mapper.writeValueAsString(electionMessage));
    }

    public void receiveElectionMessage(InetAddress mid, boolean isLeader) throws IOException {

        this.pid = ServerConfigurationSingleton.getInstance().getServerAddress();
        ArrayList al = new ArrayList();
        al.add(mid);
        al.add(this.pid);

        Collections.sort(al, new InetAddressComparator());

        if(isLeader) {
            //if mid = pid if the message is its own election message, declare itselfe as a leader
            ServerConfigurationSingleton.getInstance().setLeader(mid);

            if(!ServerConfigurationSingleton.getInstance().getServerAddress().equals(mid)) {
                //ServerConfigurationSingleton.getInstance().setIsLeader(true);
                sendLeaderMessage(mid, isLeader);
            }
            else{
                ObjectMapper mapper = new ObjectMapper();
                MulticastSender multicastSender = new MulticastSender(Constants.CLIENT_MULTICAST_PORT);
                Message message = new ClientMessage(ClientMessageType.LEADER_ANNOUNCEMENT, "broadcast_response");
                multicastSender.sendMulticast(mapper.writeValueAsString(message),Constants.CLIENT_MULTICAST_PORT);
                multicastSender.close();

            }

//            System.out.println("I am the Leader1: "+ServerConfigurationSingleton.getInstance().getIsLeader() +".   The Leader is: "+ServerConfigurationSingleton.getInstance().getLeader());
        }

        else if (mid.equals(this.pid)) {
                //if mid = pid if the message is its own election message, declare itselfe as a leader
                ServerConfigurationSingleton.getInstance().setIsLeader(true);
                sendLeaderMessage(this.pid, true);

            } else if (mid.equals(al.get(0))) {
                    //if mid < pid send E(pid, false) to the left
                    sendElectionMessage(pid, false);

                } else {
                    //if mid > pid send E(pid, false) to the left"
                    sendElectionMessage(mid, isLeader);
                }

    }

    public void startElection() {
        formRing();
        ServerConfigurationSingleton.getInstance().setIsLeader(false);
        MessageSender messageSender;
        try {
            messageSender = new MessageSender(get_neighbour());

            ObjectMapper mapper = new ObjectMapper();

            ElectionMessage electionMessage = new ElectionMessage(ServerMessageType.ELECTION_MESSAGE,"election_message", ServerConfigurationSingleton.getInstance().getServerAddress(), false);

            String jsonString = mapper.writeValueAsString(electionMessage);
            messageSender.sendMessage(jsonString);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        formRing();
        ServerConfigurationSingleton.getInstance().setIsLeader(false);
        MessageSender messageSender;
        try {
            messageSender = new MessageSender(get_neighbour());

            ObjectMapper mapper = new ObjectMapper();

            ElectionMessage electionMessage = new ElectionMessage(ServerMessageType.ELECTION_MESSAGE,"election_message", ServerConfigurationSingleton.getInstance().getServerAddress(), false);

            String jsonString = mapper.writeValueAsString(electionMessage);
            messageSender.sendMessage(jsonString);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


