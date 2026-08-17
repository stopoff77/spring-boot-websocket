package com.example.websocket.biz.health.controller;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.websocket.biz.dual.dto.UserSessionStatus;
import com.example.websocket.configuration.websocket.session.manager.DualSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RedisSessionCheckController {

    private final DualSessionManager    sessionManager;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 클라이언트 요청으로 소켓 연결 상태 실시간 점검 (Redis 전역 조회)
     * Destination: /pub/session/check
     */
    @MessageMapping("/session/check")
    public void checkSessionStatus(@Header("userId") String userId) {
        log.info("🔍 [수동 연결 상태 조회 요청] User: {}", userId);

        // Redis 전역 세션 상태 조회 (메서드명 통일: getSessionStatus)
        UserSessionStatus status = sessionManager.getSessionStatus(userId);

        // 앱/TM AP 공통 상태 수신 채널(/sub/session/status/{userId})로 응답 전송
        messagingTemplate.convertAndSend("/sub/session/status/" + userId, status);

        log.info("📤 [연결 상태 응답 발송 완료] User: {}, Status: {}", userId, status.statusSummary());
    }
}
