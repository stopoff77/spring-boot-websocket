package com.example.websocket.configuration.websocket.session.service;


import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisSessionService {

    private final StringRedisTemplate redisTemplate;
    private static final String       PREFIX = "ws:session:";

    // 전역 세션 등록 (24시간 TTL 설정)
    public void registerGlobalSession(String userId, String clientType) {
        String key = PREFIX + userId + ":" + clientType;
        redisTemplate.opsForValue().set(key, "CONNECTED", Duration.ofHours(24));
    }

    // 전역 세션 삭제
    public void removeGlobalSession(String userId, String clientType) {
        String key = PREFIX + userId + ":" + clientType;
        redisTemplate.delete(key);
    }

    // 전역 세션 연결 여부 확인
    public boolean isGlobalConnected(String userId, String clientType) {
        String key = PREFIX + userId + ":" + clientType;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
