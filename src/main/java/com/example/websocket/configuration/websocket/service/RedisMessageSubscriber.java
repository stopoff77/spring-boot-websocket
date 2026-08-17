package com.example.websocket.configuration.websocket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

import com.example.websocket.configuration.websocket.session.dto.OutboundRelayDto;
import com.example.websocket.configuration.websocket.session.manager.OutboundSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    @Value("${server.id:SERVER_A}")
    private String myServerId;

    private final OutboundSessionManager sessionManager;
    private final ObjectMapper           objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String           body     = new String(message.getBody());
            OutboundRelayDto relayDto = objectMapper.readValue(body, OutboundRelayDto.class);

            // 전달받은 메시지의 수신 대상 서버가 '나'인 경우에만 실제 소켓 발송 수행
            if (myServerId.equals(relayDto.targetServerId())) {
                StompSession session = sessionManager.getLocalSession(relayDto.targetSystemId());
                if (session != null && session.isConnected()) {
                    session.send("/pub/call/connect", relayDto.message());
                    log.info("✔ [Pub/Sub 수신 후 소켓 발송 완료] TargetSystem: {}", relayDto.targetSystemId());
                }
            }
        } catch (Exception e) {
            log.error("Redis Subscriber 처리 에러", e);
        }
    }
}
