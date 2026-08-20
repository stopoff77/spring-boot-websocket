package com.example.websocket.biz.chat.dto;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

//통화 메시지 DTO
public record CallMessage(
        String callerId, // 발신자 ID
        String receiverId, // 수신자 ID
        String type, // CONNECT / END
        String message // 전달 메시지
) {

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
