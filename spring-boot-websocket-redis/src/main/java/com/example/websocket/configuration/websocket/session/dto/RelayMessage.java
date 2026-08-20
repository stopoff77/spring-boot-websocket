package com.example.websocket.configuration.websocket.session.dto;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.example.websocket.biz.Const;
import com.example.websocket.biz.dual.dto.DualCallMessage;

public record RelayMessage(
        String targetUserId,
        String targetClientType, // "TM" 또는 "APP"
        DualCallMessage payload) {


    public String destination() {
        StringBuilder builder = new StringBuilder();
        builder.append("/sub");

        if (Const.TM.equals(targetClientType)) {
            builder.append("/tm");
        } else {
            builder.append("/app");
        }

        builder.append("/").append(targetUserId);

        return builder.toString();
    }


    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
