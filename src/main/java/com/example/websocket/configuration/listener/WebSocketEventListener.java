package com.example.websocket.configuration.listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.websocket.configuration.manager.UserSessionManager;

import lombok.extern.slf4j.Slf4j;


// 웹소켓 연결시 발생 이벤트
@Slf4j
@Component
public class WebSocketEventListener {

    private final UserSessionManager sessionManager;


    public WebSocketEventListener(UserSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }


    /**
     * 1. 연결 요청 시점 (Connect Handshake 단계)
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId(); // 세션 ID 추출
        log.info("[Connect 요청] Session ID: {}", sessionId);
        log.info("Message: {}", event.getMessage());
    }

    /**
     * 2. STOMP 연결 완결 시점 (Connected 프레임 생성 완료 후)
     */
    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId(); // 세션 ID 추출
        log.info("[Connected 완료] 웹소켓 연결 성공 - Session ID: {}", sessionId);
        log.info("Message: {}", event.getMessage());
    }


    /**
     * STOMP 연결 해제
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String              sessionId      = headerAccessor.getSessionId();

        // 매핑 삭제 후 튕긴 사용자 ID 반환받음
        String removedUserId = sessionManager.removeSession(sessionId);

        log.info("[소켓 연결 해제] Session: {}", sessionId);
        log.info("Message: {}", event.getMessage());
        if (removedUserId != null) {
            log.info("[소켓 연결 해제 & 매핑 제거] User: {} (Session: {})", removedUserId, sessionId);
        }
    }
}
