package com.example.websocket.configuration.websocket.session.dto;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record UserSession(
        String userId,
        String appSessionId,
        String tmSessionId) {

    // 세션 정보 변경 시 새로운 record 객체를 생성하는 팩토리 메서드
    public UserSession withAppSessionId(String newAppSessionId) {
        return new UserSession(this.userId, newAppSessionId, this.tmSessionId);
    }

    public UserSession withTmSessionId(String newTmSessionId) {
        return new UserSession(this.userId, this.appSessionId, newTmSessionId);
    }

    // 두 세션 모두 종료되었는지 확인
    public boolean isEmpty() {
        return appSessionId == null && tmSessionId == null;
    }


    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
