package com.example.websocket.biz.call.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.example.websocket.biz.call.dto.CallMessage;
import com.example.websocket.biz.dual.dto.UserInfo;
import com.example.websocket.configuration.websocket.session.manager.UserSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CallController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserSessionManager           sessionManager;

    // 네이티브 앱이 연결 직후 /pub/user/init 으로 본인 정보를 보냄
//    @MessageMapping("/user/init")
//    public void initUserInfo(UserInfo userInfo, StompHeaderAccessor headerAccessor) {
//
//        String websocketSessionId = headerAccessor.getSessionId();
//        String userId             = userInfo.userId();
//
//        // 매핑 저장
//        sessionManager.registerSession(userId, websocketSessionId);
//
//        log.info("[메시지 수신을 통한 매핑] User: {} ({}) <-> Session: {}",
//                userInfo.userName(), userId, websocketSessionId);
//    }

    /**
     * 1. 웹소켓 연결 직후 사용자 정보 초기화 및 소켓 세션 매핑
     * 요청 경로: /pub/user/init
     */
    @MessageMapping("/user/init")
    public void initUserInfo(UserInfo userInfo, StompHeaderAccessor headerAccessor) {
        String stompSessionId = headerAccessor.getSessionId();

        // 사용자 ID <-> 소켓 세션 ID 매핑
        sessionManager.registerSession(userInfo.userId(), stompSessionId);

        log.info("[사용자 초기화 완료] User ID: {}, Name: {} <-> STOMP Session ID: {}",
                userInfo.userId(), userInfo.userName(), stompSessionId);
    }

    /**
     * 2. 통화 연결 버튼 클릭 시 호출
     * 요청 경로: /pub/call/connect
     */
    @MessageMapping("/call/connect")
    public void handleCallConnect(CallMessage callMessage, StompHeaderAccessor headerAccessor) {
        String stompSessionId = headerAccessor.getSessionId();

        log.info("[통화 연결 요청] Caller: {} -> Receiver: {} (Session ID: {})",
                callMessage.callerId(), callMessage.receiverId(), stompSessionId);

        // 상대방(receiverId)의 개인 알림 채널(/sub/call/{receiverId})로 통화 요청 전송
        messagingTemplate.convertAndSend("/sub/call/" + callMessage.receiverId(), callMessage);
    }

    /**
     * 3. 통화 종료 버튼 클릭 시 호출
     * 요청 경로: /pub/call/end
     */
    @MessageMapping("/call/end")
    public void handleCallEnd(CallMessage callMessage, StompHeaderAccessor headerAccessor) {
        String stompSessionId = headerAccessor.getSessionId();

        log.info("[통화 종료 요청] Caller: {} -> Receiver: {} (Session ID: {})",
                callMessage.callerId(), callMessage.receiverId(), stompSessionId);

        // 상대방(receiverId)의 개인 알림 채널(/sub/call/{receiverId})로 통화 종료 전송
        messagingTemplate.convertAndSend("/sub/call/" + callMessage.receiverId(), callMessage);
    }
}
