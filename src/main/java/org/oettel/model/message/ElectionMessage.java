package org.oettel.model.message;

import java.net.InetAddress;

public class ElectionMessage extends ServerMessage{

    private InetAddress mid;
    private boolean isLeader;

    public ElectionMessage() {
        super();
    }

    public ElectionMessage(ServerMessageType mt, String content) {
        super(mt, content);
    }


    public ElectionMessage(ServerMessageType mt, String content, InetAddress mid, boolean isLeader) {
        super(mt, content);
        this.mid = mid;
        this.isLeader = isLeader;
    }


    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }


    public InetAddress getMid() {
        return mid;
    }

    public void setMid(InetAddress mid) {
        this.mid = mid;
    }

}
