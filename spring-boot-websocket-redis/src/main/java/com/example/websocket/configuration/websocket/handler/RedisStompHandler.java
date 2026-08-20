package com.example.websocket.configuration.websocket.handler;


import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.example.websocket.configuration.websocket.session.manager.UserSessionManager;
import com.example.websocket.configuration.websocket.session.service.RedisSessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStompHandler implements ChannelInterceptor {

    private final UserSessionManager  localSessionManager;
    private final RedisSessionService redisSessionService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            String sessionId = accessor.getSessionId();

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String userId     = accessor.getFirstNativeHeader("userId");
                String clientType = accessor.getFirstNativeHeader("clientType"); // "APP" 또는 "TM"

                if (userId != null && clientType != null) {
                    // 1. 로컬 인메모리 세션 등록
                    localSessionManager.registerSession(userId, sessionId, clientType);
                    // 2. Redis 전역 세션 등록
                    redisSessionService.registerGlobalSession(userId, clientType);

                    accessor.setUser(() -> userId);

                    // 세션 해제 시 참조를 위한 세션 속성 저장
                    accessor.getSessionAttributes().put("userId", userId);
                    accessor.getSessionAttributes().put("clientType", clientType);
                }
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                if (sessionId != null) {
                    String userId     = (String) accessor.getSessionAttributes().get("userId");
                    String clientType = (String) accessor.getSessionAttributes().get("clientType");

                    // 1. 로컬 인메모리 세션 제거
                    localSessionManager.removeSession(sessionId);

                    // 2. Redis 전역 세션 제거
                    if (userId != null && clientType != null) {
                        redisSessionService.removeGlobalSession(userId, clientType);
                    }
                }
            }
        }

        return message;
    }
}
