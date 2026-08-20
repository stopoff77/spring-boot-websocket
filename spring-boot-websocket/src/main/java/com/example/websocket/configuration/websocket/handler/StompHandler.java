package com.example.websocket.configuration.websocket.handler;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import com.example.websocket.configuration.websocket.session.manager.UserSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


// 웹소켓 연결시 사용자 정보 받는 역할
// 웹소켓 연결시 헤더에 정보를 담아 보내야 함
// user.html 소켓 연결 참조
@Slf4j
//@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final UserSessionManager sessionManager;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        log.debug("accessor {}", accessor);
        log.debug("accessor {}", accessor.getCommand());
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 1. 네이티브 앱에서 보내온 사용자 식별자 (또는 JWT에서 추출한 userId)
            String userId   = accessor.getFirstNativeHeader("userId");
            String userName = accessor.getFirstNativeHeader("userName");

            // 2. 서버가 자동 생성한 웹소켓 세션 ID
            String websocketSessionId = accessor.getSessionId();
            log.debug("websocketSessionId {}", websocketSessionId);

            if (userId != null && websocketSessionId != null) {
                // 3. 사용자 정보 <-> 소켓 세션 ID 매핑 저장
                sessionManager.registerSession(userId, websocketSessionId);

                // 4. Spring STOMP 내부 Principal 등록 (선택 사항)
                accessor.setUser(() -> userId);

                log.info("[소켓 연결 매핑 완료] User: {}/{} <-> WS Session: {}", userId, userName, websocketSessionId);
            }
        }

        return message;
    }
}
