package com.example.websocket.configuration.websocket.session.dto;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.example.websocket.biz.dual.dto.DualCallMessage;

public record RelayMessage(
        String targetUserId,
        String targetClientType, // "TM" 또는 "APP"
        DualCallMessage payload) {


    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
