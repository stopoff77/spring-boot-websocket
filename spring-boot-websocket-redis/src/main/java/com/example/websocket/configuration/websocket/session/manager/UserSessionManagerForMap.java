package com.example.websocket.configuration.websocket.session.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class UserSessionManagerForMap {

    // Key: userId, Value: WebSocket Session ID (1인당 1기기 기준)
    private final Map<String, String> userToSessionMap = new ConcurrentHashMap<>();

    // Key: WebSocket Session ID, Value: userId (역방향 조회 및 Disconnect 처리용)
    private final Map<String, String> sessionToUserMap = new ConcurrentHashMap<>();

    // 매핑 등록
    public void registerSession(String userId, String sessionId) {
        userToSessionMap.put(userId, sessionId);
        sessionToUserMap.put(sessionId, userId);
    }

    // 세션 ID로 userId 조회
    public String getUserId(String sessionId) {
        return sessionToUserMap.get(sessionId);
    }

    // userId로 세션 ID 조회 (특정 사용자에게 1:1 메시지 보낼 때 사용)
    public String getSessionId(String userId) {
        return userToSessionMap.get(userId);
    }

    // 연결 종료 시 매핑 제거
    public String removeSession(String sessionId) {
        String userId = sessionToUserMap.remove(sessionId);
        if (userId != null) {
            userToSessionMap.remove(userId);
        }
        return userId;
    }
}
