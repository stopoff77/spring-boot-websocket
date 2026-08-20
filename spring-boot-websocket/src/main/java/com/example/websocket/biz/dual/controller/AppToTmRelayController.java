package com.example.websocket.biz.dual.controller;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.example.websocket.biz.dual.dto.DualCallMessage;
import com.example.websocket.biz.dual.dto.UserInfo;
import com.example.websocket.configuration.websocket.session.manager.UserSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AppToTmRelayController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserSessionManager    sessionManager;


    /**
     * 1. 웹소켓 연결 직후 사용자 정보 초기화 및 소켓 세션 매핑
     * 요청 경로: /pub/user/init
     */
    @MessageMapping("/user/init2")
    public void initUserInfo(UserInfo userInfo, StompHeaderAccessor headerAccessor) {
        String stompSessionId = headerAccessor.getSessionId();

        // 사용자 ID <-> 소켓 세션 ID 매핑
//        sessionManager.registerSession(userInfo.userId(), stompSessionId, userInfo.deviceType());
        if (userInfo.userId() != null && userInfo.deviceType() != null) {
            sessionManager.registerSession(userInfo.userId(), stompSessionId);
//            headerAccessor.setUser(() -> userId);
        }

        log.info("[사용자 초기화 완료] User ID: {}, Name: {} <-> STOMP Session ID: {}",
                userInfo.userId(), userInfo.userName(), stompSessionId);
    }

    /**
     * 앱에서 들어온 메시지를 동일한 사용자의 TM AP 세션으로 라우팅
     * Destination: /pub/app/to-tm
     */
    @MessageMapping("/app/to-tm")
    public void relayAppMessageToTm(@Payload DualCallMessage payload,
            @Header("userId") String userId) {

        log.info("📩 [앱 메시지 수신] User: {}, Payload: {}", userId, payload);

        // 1. 해당 사용자의 TM AP 세션 ID 조회
        String tmSessionId = sessionManager.getTmSessionId(userId);

        if (tmSessionId == null) {
            log.warn("⚠️ [토스 실패] 해당 사용자({})의 TM AP 세션이 존재하지 않습니다.", userId);

            // 1. 실패 메시지 전송용 객체 생성 (CallMessage record 재활용)
            DualCallMessage failResponse = new DualCallMessage(
                    "SYSTEM",
                    "FAIL",
                    "TM 상담원 소켓 연결이 존재하지 않아 메시지 전달에 실패했습니다.");

            // 2. 앱 전용 수신 채널(/sub/app/{userId})로 실패 응답 발송
            messagingTemplate.convertAndSend("/sub/app/" + userId, failResponse);

            return;
        }

        // 2. 단일 타깃 세션 지정을 위한 HeaderAccessor 설정
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(tmSessionId);
        headerAccessor.setLeaveMutable(true);

        // 3. TM AP 전용 개인 큐로 메시지 토스
//        messagingTemplate.convertAndSendToUser(
//                tmSessionId,
//                "/sub/tm/queue",
//                payload,
//                headerAccessor.getMessageHeaders());

        // /sub/tm/{userId} 채널로 직접 메시지 발송
        messagingTemplate.convertAndSend("/sub/tm/" + userId, payload);

        log.info("▶ [TM AP로 토스 완료] Target User: {}, TmSessionId: {}", userId, tmSessionId);
    }
}
