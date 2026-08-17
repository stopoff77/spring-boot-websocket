package com.example.websocket.configuration.redis;


import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.websocket.configuration.websocket.session.dto.RelayMessage;
import com.example.websocket.configuration.websocket.session.manager.DualSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber {

    private final ObjectMapper          objectMapper;
    private final DualSessionManager    localSessionManager;
    private final SimpMessagingTemplate messagingTemplate;

    public void onMessage(String messageJson) {
        try {
            RelayMessage relayMessage     = objectMapper.readValue(messageJson, RelayMessage.class);
            String       targetUserId     = relayMessage.targetUserId();
            String       targetClientType = relayMessage.targetClientType();

            // 1. TM AP로 전달해야 하는 경우
            if ("TM".equalsIgnoreCase(targetClientType)) {
                String localTmSessionId = localSessionManager.getTmSessionId(targetUserId);

                // 본 WAS에 TM AP 웹소켓 세션이 존재하는 경우에만 실제 발송
                if (localTmSessionId != null) {
                    messagingTemplate.convertAndSend("/sub/tm/" + targetUserId, relayMessage.payload());
                    log.info("🎯 [Redis Sub -> 로컬 TM 토스 완료] User: {}, WAS Local Session Found", targetUserId);
                }
            }
            // 2. 앱(APP)으로 실패/응답 메시지를 전달해야 하는 경우
            else if ("APP".equalsIgnoreCase(targetClientType)) {
                String localAppSessionId = localSessionManager.getAppSessionId(targetUserId);

                if (localAppSessionId != null) {
                    messagingTemplate.convertAndSend("/sub/app/" + targetUserId, relayMessage.payload());
                    log.info("🎯 [Redis Sub -> 로컬 APP 응답 완료] User: {}", targetUserId);
                }
            }
        } catch (Exception e) {
            log.error("❌ RedisSubscriber 메시지 처리 중 오류 발생", e);
        }
    }
}
