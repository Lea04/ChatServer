package org.oettel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oettel.businesslogic.*;
import org.oettel.configuration.ServerConfigurationSingleton;
import org.oettel.listener.BroadcastListener;
import org.oettel.listener.MessageListener;

import org.oettel.listener.MulticastListener;
import org.oettel.model.message.Message;
import org.oettel.model.message.ServerMessage;
import org.oettel.model.message.ServerMessageType;
import org.oettel.sender.BroadcastSender;
import org.oettel.sender.MessageSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.oettel.configuration.Constants.SERVER_PORT;

public class Main {

    /**
     * Starting point of server
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        initialConfigurationOfServer(SERVER_PORT);
        startingListener();
        sendInitialBroadcast();
        sendHeartbeat();
    }

    /**
     * Method for configuring the Server startup.
     *
     * @param serverPort for server.
     */
    private static void initialConfigurationOfServer(final int serverPort) {
        System.out.println("#### initialization of server ####");
        ServerConfigurationSingleton.getInstance().setServerPort(serverPort);
        try {
            ServerConfigurationSingleton.getInstance().setServerAddress(InetAddress.getByAddress(InetAddress.getLocalHost().getAddress()));
            ServerConfigurationSingleton.getInstance().setSequenceNumber(0);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println("#### initialization of server completed ####\n");
    }


    /**
     * starts the listeners for broadcast, unicast and multicast messages.
     */

    private static void startingListener() {
        System.out.println("#### starting listener ####");
        ExecutorService pool = Executors.newFixedThreadPool(20);

        try {
            BroadcastListener broadcastListener = new BroadcastListener(new BroadcastServerService(), new MulticastServerService());
            MessageListener messageListener = new MessageListener(new UnicastServerService());
            MulticastListener multicastListener = new MulticastListener();
            pool.execute(broadcastListener);
            pool.execute(messageListener);
            pool.execute(multicastListener);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sends the initial broadcast and checks for other servers.
     *
     * @throws SocketException
     * @throws UnknownHostException
     */
    private static void sendInitialBroadcast() throws SocketException, UnknownHostException {
        BroadcastSender broadCastSender = new BroadcastSender();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Message message = new ServerMessage(ServerMessageType.BROADCAST, "broadcast");
            broadCastSender.sendEcho(mapper.writeValueAsString(message));

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\nSend initial Broadcast");
    }

    /**
     * heartbeat started to all servers.
     *
     * @throws IOException
     */
    private static void sendHeartbeat() throws IOException {
        //The initial heartbeat is written in the run method of the message sender.
        ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
        MessageSender messageSender = new MessageSender();
        pool.schedule(messageSender,20,TimeUnit.SECONDS);

        ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(10);
        ScheduledHeartbeatService scheduledHeartbeatService = new ScheduledHeartbeatService();
        scheduledPool.scheduleAtFixedRate(scheduledHeartbeatService, 30,10, TimeUnit.SECONDS);
    }

}
