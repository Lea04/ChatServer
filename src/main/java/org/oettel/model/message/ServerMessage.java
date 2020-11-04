package org.oettel.model.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.oettel.model.vectorclock.VectorClockEntry;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = ElectionMessage.class, name = "ElectionMessage")
        })

public class ServerMessage extends Message {
    private ServerMessageType serverMessageType;
    private List<VectorClockEntry> vectorClockEntryList;

    public ServerMessage() {
    }

    public ServerMessage(String content) {
        super(MessageType.SERVER_MESSAGE, content);
    }

    public ServerMessage(ServerMessageType serverMessageType, String content) {
        super(MessageType.SERVER_MESSAGE, content);
        this.serverMessageType = serverMessageType;
    }

    public ServerMessage(ServerMessageType serverMessageType, String content, List<VectorClockEntry> vectorClockEntryList) {
        super(MessageType.SERVER_MESSAGE, content);
        this.serverMessageType = serverMessageType;
        this.vectorClockEntryList = vectorClockEntryList;
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public void setServerMessageType(ServerMessageType serverMessageType) {
        this.serverMessageType = serverMessageType;
    }

    public List<VectorClockEntry> getVectorClockEntryList() {
        return vectorClockEntryList;
    }
}
