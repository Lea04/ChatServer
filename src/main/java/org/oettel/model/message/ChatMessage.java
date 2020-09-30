package org.oettel.model.message;

import java.net.InetAddress;

import java.util.HashMap;
import java.util.Map;


public class ChatMessage extends ClientMessage{

    private Map<InetAddress,Integer> vectorClock  = new HashMap<InetAddress, Integer>();


    public ChatMessage() {
        super();
    }

    public ChatMessage(ClientMessageType mt, String content) {
        super(mt, content);
    }


    public ChatMessage(ClientMessageType mt, String content, Map<InetAddress,Integer> vectorClock) {
        super(mt, content);
        this.vectorClock = vectorClock;
    }

    public Map<InetAddress, Integer> getVectorClock() {
        return vectorClock;
    }

    public void setVectorClock(Map<InetAddress, Integer> vectorClock) {
        this.vectorClock = vectorClock;
    }
}
