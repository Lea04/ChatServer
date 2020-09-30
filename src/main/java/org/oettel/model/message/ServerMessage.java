package org.oettel.model.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)

@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = ElectionMessage.class, name = "ElectionMessage")
        })

public class ServerMessage extends Message {
    private ServerMessageType serverMessageType;

    public ServerMessage() {
    }

    public ServerMessage(String content) {
        super(MessageType.SERVER_MESSAGE, content);
    }

    public ServerMessage(ServerMessageType serverMessageType, String content) {
        super(MessageType.SERVER_MESSAGE, content);
        this.serverMessageType = serverMessageType;
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public void setServerMessageType(ServerMessageType serverMessageType) {
        this.serverMessageType = serverMessageType;
    }
}
