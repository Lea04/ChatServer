package org.oettel.configuration;

import org.oettel.model.vectorclock.VectorClockEntry;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class  ServerConfigurationSingleton {
    private static ServerConfigurationSingleton instance;
    private int serverPort;
    private InetAddress serverAddress;
    private InetAddress leader;
    private boolean isLeader;
    private int lastReplicated;
    private int sequenceNumber;
    private int queueIdCounter = 0;
    HashMap<Integer, String> holdbackQueue = new HashMap<Integer, String>();




    private final List<InetAddress> replicaServer = new ArrayList<>();

    private ServerConfigurationSingleton() {

    }


    public static ServerConfigurationSingleton getInstance() {
        if (ServerConfigurationSingleton.instance == null) {
            ServerConfigurationSingleton.instance = new ServerConfigurationSingleton();
        }
        return ServerConfigurationSingleton.instance;
    }

    public InetAddress getLeader() {return leader;}

    public void setLeader(InetAddress leader) {
        this.leader = leader;
    }

    public boolean getIsLeader() {
        return isLeader;
    }

    public void setIsLeader(boolean leader) {
        isLeader = leader;
    }

    public void setServerPort(final int serverPort) {
        this.serverPort = serverPort;
        System.out.println("Configure Server Port: " + this.serverPort);
    }

    public void setServerAddress(final InetAddress serverAddress) {
        this.serverAddress = serverAddress;
        System.out.println("Configure Server Address: " + this.serverAddress);
    }

    public int getServerPort() {
        return serverPort;
    }

    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public void addReplicaToReplicaList(final InetAddress replicaAddress) {
        replicaServer.add(replicaAddress);
    }
    
    public void removeReplicaFromReplicaList(final InetAddress replicaAddress) {
        replicaServer.remove(replicaServer.indexOf(replicaAddress));
    }

    public List<InetAddress> getReplicaServer() {
        return replicaServer;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void addMessageToHoldbackQueue(String content) {
        this.queueIdCounter = queueIdCounter + 1;
        holdbackQueue.put(this.queueIdCounter, content);
    }
}

