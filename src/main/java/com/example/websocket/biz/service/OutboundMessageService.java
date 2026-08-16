package com.example.websocket.biz.service;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Service;

import com.example.websocket.biz.dto.CallMessage;
import com.example.websocket.biz.dto.OutboundRelayDto;
import com.example.websocket.configuration.manager.OutboundSessionManager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundMessageService {

    private final String myServerId;

    private final OutboundSessionManager sessionManager;
    private final StringRedisTemplate    redisTemplate;
    private final ObjectMapper           objectMapper;

    private static final String REDIS_PUBSUB_TOPIC = "ws-outbound-send-channel";

    @PostConstruct
    private void init() {
        //
        log.debug("myServerId {}", myServerId);
    }

    /**
     * 타깃 시스템으로 메시지 발송 (이중화 처리)
     */
    public void sendMessageToTarget(String targetSystemId, CallMessage message) {
        // 1. 레디스에서 해당 타깃 시스템이 어느 서버에 연결되어 있는지 조회
        String targetServerId = sessionManager.getConnectedServerId(targetSystemId);

        if (targetServerId == null) {
            log.warn("⚠️ [{}] 타깃 시스템 연결 정보가 없습니다.", targetSystemId);
            return;
        }

        // 2-A. 현재 서버에 연결된 소켓인 경우 -> 직접 즉시 발송
        if (myServerId.equals(targetServerId)) {
            StompSession session = sessionManager.getLocalSession(targetSystemId);
            if (session != null && session.isConnected()) {
                session.send("/pub/call/connect", message);
                log.info("▶ [로컬 직접 발송 완료] Target: {}", targetSystemId);
            }
        }
        // 2-B. 다른 서버에 연결된 소켓인 경우 -> Redis Pub/Sub으로 해당 서버에 발송 위임
        else {
            try {
                // 발송 요청 정보 패키징
                OutboundRelayDto relayDto    = new OutboundRelayDto(targetServerId, targetSystemId, message);
                String           jsonMessage = objectMapper.writeValueAsString(relayDto);

                // 레디스 채널로 발행
                redisTemplate.convertAndSend(REDIS_PUBSUB_TOPIC, jsonMessage);
                log.info("📡 [Redis Pub/Sub 브로드캐스트] TargetServer: {}, TargetSystem: {}", targetServerId,
                        targetSystemId);
            } catch (Exception e) {
                log.error("Pub/Sub 전송 실패", e);
            }
        }
    }
}
