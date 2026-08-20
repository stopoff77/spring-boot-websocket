package com.example.websocket.configuration.websocket.handler;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.example.websocket.configuration.websocket.session.manager.UserSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DualStompHandler implements ChannelInterceptor {

    private final UserSessionManager sessionManager;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            String sessionId = accessor.getSessionId();

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String userId     = accessor.getFirstNativeHeader("userId");
                String clientType = accessor.getFirstNativeHeader("clientType"); // "APP" 또는 "TM"
                log.debug("user is {}/{}", userId, clientType);

                if (userId != null && clientType != null) {
                    sessionManager.registerSession(userId, sessionId);
                    accessor.setUser(() -> userId);
                }
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                if (sessionId != null) {
                    sessionManager.removeSession(sessionId);
                }
            }
        }

        return message;
    }
}
