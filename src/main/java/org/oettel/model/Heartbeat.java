package org.oettel.model;

import java.net.InetAddress;

public class Heartbeat {
    InetAddress inetAddress;
    boolean isHeartbeatOK;

    public Heartbeat(InetAddress inetAddress, boolean isHeartbeatOK) {
        this.inetAddress = inetAddress;
        this.isHeartbeatOK = isHeartbeatOK;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public boolean getIsHeartbeatOK() {
        return isHeartbeatOK;
    }

    public void setHeartbeatOK(boolean heartbeatOK) {
        isHeartbeatOK = heartbeatOK;
    }

}
