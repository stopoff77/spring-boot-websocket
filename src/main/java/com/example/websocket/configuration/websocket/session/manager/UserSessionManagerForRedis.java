package com.example.websocket.configuration.websocket.session.manager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.websocket.biz.dual.dto.UserSessionStatus;
import com.example.websocket.configuration.websocket.session.dto.UserSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSessionManagerForRedis {

    private final StringRedisTemplate redisTemplate;

    // 로컬 WAS 메모리 세션 관리 (userId <-> UserSession)
    private final Map<String, UserSession> userSessionMap = new ConcurrentHashMap<>();

    // 로컬 WAS 메모리 빠른 세션 역조회용 (sessionId <-> userId)
    private final Map<String, String> sessionToUserMap = new ConcurrentHashMap<>();

    private static final String REDIS_KEY_PREFIX = "WS_SESSION:";

    /**
     * 세션 등록 (로컬 메모리 + Redis 전역 세션 동시 등록)
     * clientType: "APP" 또는 "TM"
     */
    public void registerSession(String userId, String sessionId, String clientType) {
        String upperType = clientType.toUpperCase();

        // 1. 로컬 WAS 메모리 세션 저장
        userSessionMap.compute(userId, (key, existingSession) -> {
            UserSession session = (existingSession != null)
                    ? existingSession
                    : new UserSession(userId, null, null);

            if ("APP".equals(upperType)) {
                log.info("📱 [앱 세션 매핑 완료] User: {} <-> AppSession: {}", userId, sessionId);
                return session.withAppSessionId(sessionId);
            } else if ("TM".equals(upperType)) {
                log.info("🎧 [TM AP 세션 매핑 완료] User: {} <-> TmSession: {}", userId, sessionId);
                return session.withTmSessionId(sessionId);
            }
            return session;
        });

        sessionToUserMap.put(sessionId, userId);

        // 2. Redis 전역 세션 등록 (Key: WS_SESSION:{userId}, HashField: APP 또는 TM)
        redisTemplate.opsForHash().put(REDIS_KEY_PREFIX + userId, upperType, sessionId);
        log.info("🌐 [Redis 전역 세션 등록] User: {}, Type: {}", userId, upperType);
    }

    /**
     * 로컬 WAS 메모리의 TM AP 세션 ID 조회
     */
    public String getTmSessionId(String userId) {
        UserSession session = userSessionMap.get(userId);
        return (session != null) ? session.tmSessionId() : null;
    }

    /**
     * 로컬 WAS 메모리의 앱 세션 ID 조회
     */
    public String getAppSessionId(String userId) {
        UserSession session = userSessionMap.get(userId);
        return (session != null) ? session.appSessionId() : null;
    }

    /**
     * Redis 전역 클러스터 내 특정 사용자의 TM 세션 존재 여부 확인
     */
    public boolean isTmConnectedGlobal(String userId) {
        Boolean hasKey = redisTemplate.opsForHash().hasKey(REDIS_KEY_PREFIX + userId, "TM");
        return Boolean.TRUE.equals(hasKey);
    }

    /**
     * 세션 종료 시 처리 (로컬 메모리 + Redis 전역 세션 동시 삭제)
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
                redisTemplate.opsForHash().delete(REDIS_KEY_PREFIX + userId, "APP");
                log.info("❌ [앱 세션 해제] User: {}", userId);
            } else if (sessionId.equals(existingSession.tmSessionId())) {
                updatedSession = existingSession.withTmSessionId(null);
                redisTemplate.opsForHash().delete(REDIS_KEY_PREFIX + userId, "TM");
                log.info("❌ [TM 세션 해제] User: {}", userId);
            }

            // 두 세션 모두 해제되었으면 null을 반환하여 로컬 Map에서 삭제
            return updatedSession.isEmpty() ? null : updatedSession;
        });
    }

    /**
     * 특정 사용자의 전역(Redis) 연결 상태 조회
     */
    public UserSessionStatus getSessionStatus(String userId) {
        Boolean isAppConnected = redisTemplate.opsForHash().hasKey(REDIS_KEY_PREFIX + userId, "APP");
        Boolean isTmConnected  = redisTemplate.opsForHash().hasKey(REDIS_KEY_PREFIX + userId, "TM");

        return UserSessionStatus.of(
                userId,
                Boolean.TRUE.equals(isAppConnected),
                Boolean.TRUE.equals(isTmConnected));
    }

    /**
     * 관리 중인 모든 사용자의 전역(Redis) 연결 상태 목록 조회
     */
    public List<UserSessionStatus> getAllSessionStatuses() {
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        return keys.stream()
                .map(key -> key.replace(REDIS_KEY_PREFIX, ""))
                .map(this::getSessionStatus)
                .toList();
    }
}
