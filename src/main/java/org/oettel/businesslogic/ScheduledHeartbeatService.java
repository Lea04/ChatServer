package org.oettel.businesslogic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.configuration.HeartbeatListSingleton;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.model.Heartbeat;
import org.oettel.model.message.Message;
import org.oettel.model.message.ServerMessage;
import org.oettel.model.message.ServerMessageType;
import org.oettel.sender.BroadcastSender;
import org.oettel.sender.UnicastSender;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledHeartbeatService implements Runnable {
    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("~ Heartbeat");
        System.out.println("    Leader: " +ServerConfigurationSingleton.getInstance().getIsLeader()+
                "\n        leader IP: "+ServerConfigurationSingleton.getInstance().getLeader());



        HeartbeatListSingleton.getInstance().getHeartbeatList().forEach(heartbeat -> {
            //System.out.println("Heartbeatstatus: " + heartbeat.getInetAddress().toString() + " " + heartbeat.getIsHeartbeatOK());
            if(!heartbeat.getIsHeartbeatOK()){
                ServerConfigurationSingleton.getInstance().removeReplicaFromReplicaList(heartbeat.getInetAddress());
            }
        });


        /**
         * Calls the LeaderElection if the Lead server is deleted from the ReplicaList
         */
        if(!ServerConfigurationSingleton.getInstance().getReplicaServer().contains(ServerConfigurationSingleton.getInstance().getLeader())
            /*& !ServerConfigurationSingleton.getInstance().getReplicaServer().isEmpty()*/
            /*& &!ServerConfigurationSingleton.getInstance().getIsLeader() */){
            //TODO: To test
//            LeaderElectionService leaderElectionService = new LeaderElectionService();
//            leaderElectionService.startElection();

            ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
            LeaderElectionService leaderElectionService = new LeaderElectionService();
            pool.schedule(leaderElectionService,2, TimeUnit.SECONDS);


        }


        //__

        if(!ServerConfigurationSingleton.getInstance().getReplicaServer().contains(ServerConfigurationSingleton.getInstance().getServerAddress()
        )){
            //TODO: To test
//            LeaderElectionService leaderElectionService = new LeaderElectionService();
//            leaderElectionService.startElection();

            ServerConfigurationSingleton.getInstance().getReplicaServer().add(ServerConfigurationSingleton.getInstance().getServerAddress());


            ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
            LeaderElectionService leaderElectionService = new LeaderElectionService();
            pool.schedule(leaderElectionService,2, TimeUnit.SECONDS);




        }


        //__



        HeartbeatListSingleton.getInstance().setHeartbeatList();

        ServerConfigurationSingleton.getInstance().getReplicaServer().forEach(inetAddress -> {
            Heartbeat heartbeat = new Heartbeat(inetAddress, false);
            HeartbeatListSingleton.getInstance().addReplicaToHeartbeatList(heartbeat);
            try {
                Message heartbeatMessage = new ServerMessage(ServerMessageType.HEARTBEAT, "heartbeat");
                String messageJson = mapper.writeValueAsString(heartbeatMessage);
                UnicastSender unicastSender = new UnicastSender(inetAddress);
                unicastSender.sendMessage(messageJson);
                unicastSender.close();

                BroadcastSender broadcastSender = new BroadcastSender();
                broadcastSender.sendEcho(messageJson);
                broadcastSender.close();

            } catch (ConnectException e){
                //e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
        });



       /* try {
            Message heartbeatMessage = new ServerMessage(ServerMessageType.HEARTBEAT, "heartbeat");
            String messageJson = mapper.writeValueAsString(heartbeatMessage);
            BroadcastSender broadcastSender = new BroadcastSender();
            broadcastSender.sendEcho(messageJson);
            broadcastSender.close();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


    }
}
