package com.example.websocket.biz.chat.dto;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record ChatMessage(
        MessageType type,
        String roomId,
        String sender,
        String message) {
    public enum MessageType {
        ENTER, TALK, LEAVE
    }


    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
