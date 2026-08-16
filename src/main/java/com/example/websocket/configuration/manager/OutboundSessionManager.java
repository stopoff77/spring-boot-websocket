package com.example.websocket.configuration.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

@Component
public class OutboundSessionManager {

    private final String myServerId;


    // 1. 현재 서버가 소유한 물리적 StompSession 보관 (Local Memory)
    private final Map<String, StompSession> localSessionMap = new ConcurrentHashMap<>();

    private final StringRedisTemplate redisTemplate;
    private static final String       REDIS_KEY_PREFIX = "OUTBOUND_WS:";

    public OutboundSessionManager(StringRedisTemplate redisTemplate, @Qualifier("myServerId") String myServerId) {
        this.redisTemplate = redisTemplate;
        this.myServerId    = myServerId;
    }

    // 소켓 연결 성공 시 레디스와 로컬 맵에 등록
    public void registerSession(String targetSystemId, StompSession session) {
        localSessionMap.put(targetSystemId, session);

        // Redis에 "어느 서버가 이 시스템과 연결되어 있는지" 위치 정보 저장
        // Key: OUTBOUND_WS:SYSTEM_X -> Value: SERVER_A
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + targetSystemId, myServerId);
    }

    // 로컬 세션 조회
    public StompSession getLocalSession(String targetSystemId) {
        return localSessionMap.get(targetSystemId);
    }

    // 타깃 시스템이 연결된 서버 ID 조회
    public String getConnectedServerId(String targetSystemId) {
        return redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + targetSystemId);
    }

    // 연결 종료 시 정리
    public void removeSession(String targetSystemId) {
        localSessionMap.remove(targetSystemId);
        redisTemplate.delete(REDIS_KEY_PREFIX + targetSystemId);
    }
}
