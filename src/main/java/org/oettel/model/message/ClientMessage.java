package org.oettel.model.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.oettel.model.vectorclock.VectorClockEntry;

import java.util.List;

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = ChatMessage.class, name = "ChatMessage")
        })

public class ClientMessage extends Message {
    private ClientMessageType clientMessageType;
    private List<VectorClockEntry> vectorClockEntries;

    public ClientMessage() {
    }

    public ClientMessage(String content) {
        super(MessageType.CLIENT_MESSAGE, content);
    }

    public ClientMessage(ClientMessageType clientMessageType, String content) {
        super(MessageType.CLIENT_MESSAGE, content);
        this.clientMessageType = clientMessageType;
    }

    public ClientMessage(ClientMessageType clientMessageType, String content, List<VectorClockEntry> vectorClockEntryList) {
        super(MessageType.CLIENT_MESSAGE, content);
        this.clientMessageType = clientMessageType;
        this.vectorClockEntries = vectorClockEntryList;
    }

    public ClientMessageType getClientMessageType() {
        return clientMessageType;
    }

    public void setClientMessageType(ClientMessageType clientMessageType) {
        this.clientMessageType = clientMessageType;
    }

    public List<VectorClockEntry> getVectorClockEntries() {
        return vectorClockEntries;
    }

    public void setVectorClockEntries(List<VectorClockEntry> vectorClockEntries) {
        this.vectorClockEntries = vectorClockEntries;
    }
}
