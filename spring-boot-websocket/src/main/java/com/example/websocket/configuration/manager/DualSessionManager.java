package com.example.websocket.configuration.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.websocket.biz.dual.dto.UserSession;
import com.example.websocket.biz.dual.dto.UserSessionStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DualSessionManager {

    // userId <-> UserSession (record)
    private final Map<String, UserSession> userSessionMap = new ConcurrentHashMap<>();

    // sessionId <-> userId (빠른 세션 역조회용)
    private final Map<String, String> sessionToUserMap = new ConcurrentHashMap<>();

    /**
     * 세션 등록 (clientType: "APP" 또는 "TM")
     */
    public void registerSession(String userId, String sessionId, String clientType) {
        userSessionMap.compute(userId, (key, existingSession) -> {
            UserSession session = (existingSession != null)
                    ? existingSession
                    : new UserSession(userId, null, null);

            if ("APP".equalsIgnoreCase(clientType)) {
                log.info("📱 [앱 세션 매핑 완료] User: {} <-> AppSession: {}", userId, sessionId);
                return session.withAppSessionId(sessionId);
            } else if ("TM".equalsIgnoreCase(clientType)) {
                log.info("🎧 [TM AP 세션 매핑 완료] User: {} <-> TmSession: {}", userId, sessionId);
                return session.withTmSessionId(sessionId);
            }
            return session;
        });

        sessionToUserMap.put(sessionId, userId);
    }

    /**
     * TM AP 세션 ID 조회
     */
    public String getTmSessionId(String userId) {
        UserSession session = userSessionMap.get(userId);
        return (session != null) ? session.tmSessionId() : null;
    }

    /**
     * 앱 세션 ID 조회
     */
    public String getAppSessionId(String userId) {
        UserSession session = userSessionMap.get(userId);
        return (session != null) ? session.appSessionId() : null;
    }

    /**
     * 세션 종료 시 처리
     */
    public void removeSession(String sessionId) {
        String userId = sessionToUserMap.remove(sessionId);
        if (userId == null) {
            return;
        }

        userSessionMap.computeIfPresent(userId, (key, existingSession) -> {
            UserSession updatedSession = existingSession;

            if (sessionId.equals(existingSession.appSessionId())) {
                updatedSession = existingSession.withAppSessionId(null);
                log.info("❌ [앱 세션 해제] User: {}", userId);
            } else if (sessionId.equals(existingSession.tmSessionId())) {
                updatedSession = existingSession.withTmSessionId(null);
                log.info("❌ [TM 세션 해제] User: {}", userId);
            }

            // 두 세션 모두 해제되었으면 null을 반환하여 Map에서 삭제
            return updatedSession.isEmpty() ? null : updatedSession;
        });
    }


    /**
     * 특정 사용자의 연결 상태 조회
     */
    public UserSessionStatus getSessionStatus(String userId) {
        UserSession session        = userSessionMap.get(userId);
        boolean     isAppConnected = (session != null && session.appSessionId() != null);
        boolean     isTmConnected  = (session != null && session.tmSessionId() != null);

        return UserSessionStatus.of(userId, isAppConnected, isTmConnected);
    }

    /**
     * 관리 중인 모든 사용자의 연결 상태 목록 조회
     */
    public List<UserSessionStatus> getAllSessionStatuses() {
        return userSessionMap.keySet().stream()
                .map(this::getSessionStatus)
                .toList();
    }
}
