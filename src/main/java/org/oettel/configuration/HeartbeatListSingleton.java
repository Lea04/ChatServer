package org.oettel.configuration;

import org.oettel.model.Heartbeat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class HeartbeatListSingleton {
    private static HeartbeatListSingleton instance;

    private List<Heartbeat> heartbeatList  = new ArrayList<>();

    private HeartbeatListSingleton() { }

    public static HeartbeatListSingleton getInstance() {
        if (HeartbeatListSingleton.instance == null) {
            HeartbeatListSingleton.instance = new HeartbeatListSingleton();
        }
        return HeartbeatListSingleton.instance;
    }

    public void addReplicaToHeartbeatList(final Heartbeat replicaAddress) {
        heartbeatList.add(replicaAddress);
    }

    public List<Heartbeat> getHeartbeatList(){
        return heartbeatList;
    }

    public void setHeartbeatList(){
        heartbeatList  = new ArrayList<>();
    }



}
