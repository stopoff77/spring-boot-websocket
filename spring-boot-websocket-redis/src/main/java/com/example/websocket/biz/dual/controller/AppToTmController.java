package com.example.websocket.biz.dual.controller;


import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.example.websocket.biz.Const;
import com.example.websocket.biz.dual.dto.DualCallMessage;
import com.example.websocket.biz.dual.dto.UserInfo;
import com.example.websocket.configuration.redis.RedisPublisher;
import com.example.websocket.configuration.websocket.session.dto.RelayMessage;
import com.example.websocket.configuration.websocket.session.manager.UserSessionManager;
import com.example.websocket.configuration.websocket.session.service.RedisSessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


// Redis Pub/Sub(발행/구독) 패브릭과 Redis 전역 세션 상태 관리
@Slf4j
@Controller
@RequiredArgsConstructor
public class AppToTmController {

    private final RedisPublisher      redisPublisher;
    private final UserSessionManager  localSessionManager;
    private final RedisSessionService redisSessionService;


    /**
     * 1. 웹소켓 연결 직후 사용자 정보 초기화 및 소켓 세션 매핑
     * 요청 경로: /pub/user/init
     */
    @MessageMapping("/user/init3")
    public void initUserInfo(UserInfo userInfo, StompHeaderAccessor headerAccessor) {
        String stompSessionId = headerAccessor.getSessionId();

        // 사용자 ID <-> 소켓 세션 ID 매핑
        if (userInfo.userId() != null && userInfo.deviceType() != null) {
            // 1. 로컬 인메모리 세션 등록
            localSessionManager.registerSession(userInfo.userId(), stompSessionId, userInfo.deviceType());
            // 2. Redis 전역 세션 등록
            redisSessionService.registerGlobalSession(userInfo.userId(), userInfo.deviceType());

            headerAccessor.setUser(() -> userInfo.userId());

            // 세션 해제 시 참조를 위한 세션 속성 저장
            headerAccessor.getSessionAttributes().put("userId", userInfo.userId());
            headerAccessor.getSessionAttributes().put("clientType", userInfo.deviceType());
        }

        log.info("[사용자 초기화 완료] User ID: {}, Name: {} <-> STOMP Session ID: {}",
                userInfo.userId(), userInfo.userName(), stompSessionId);
    }

    @MessageMapping("/app/to-tm2")
    public void relayAppMessageToTm(@Payload DualCallMessage payload,
            @Header("userId") String userId) {

        log.info("📩 [앱 메시지 수신] User: {}, Payload: {}", userId, payload);

        // 1. Redis 전역 세션에서 TM AP 연결 여부 확인 (어느 WAS에 연결되어 있든 상관없이 검증 가능)
        boolean isTmOnline = redisSessionService.isGlobalConnected(userId, Const.TM);

        if (!isTmOnline) {
            log.warn("⚠️ [토스 실패] 전역 TM AP 세션이 존재하지 않습니다. User: {}", userId);

            DualCallMessage failResponse = new DualCallMessage(
                    "SYSTEM",
                    "FAIL",
                    "TM 상담원 소켓 연결이 존재하지 않아 메시지 전달에 실패했습니다.");

            // 실패 메시지를 Redis로 발행하여 앱이 연결된 WAS로 전달
            redisPublisher.publish(new RelayMessage(userId, Const.APP, failResponse));
            return;
        }

        // 2. TM AP가 클러스터 내(WAS 1, WAS 2 등)에 존재하므로 Redis Topic으로 메시지 전파
        RelayMessage relayMessage = new RelayMessage(userId, Const.TM, payload);
        redisPublisher.publish(relayMessage);

        log.info("▶ [Redis Pub 전파 완료] Target User: {}", userId);
    }


    @MessageMapping("/tm/to-app2")
    public void relayTmMessageToApp(@Payload DualCallMessage payload,
            @Header("userId") String userId) {

        log.info("📩 [TM 메시지 수신] User: {}, Payload: {}", userId, payload);

        // 1. Redis 전역 세션에서 TM AP 연결 여부 확인 (어느 WAS에 연결되어 있든 상관없이 검증 가능)
        boolean isAppOnline = redisSessionService.isGlobalConnected(userId, Const.APP);

        if (!isAppOnline) {
            log.warn("⚠️ [토스 실패] 전역 APP 세션이 존재하지 않습니다. User: {}", userId);

            DualCallMessage failResponse = new DualCallMessage(
                    "SYSTEM",
                    "FAIL",
                    "APP 소켓 연결이 존재하지 않아 메시지 전달에 실패했습니다.");

            // 실패 메시지를 Redis로 발행하여 앱이 연결된 WAS로 전달
            redisPublisher.publish(new RelayMessage(userId, Const.TM, failResponse));
            return;
        }

        // 2. TM AP가 클러스터 내(WAS 1, WAS 2 등)에 존재하므로 Redis Topic으로 메시지 전파
        RelayMessage relayMessage = new RelayMessage(userId, Const.APP, payload);
        redisPublisher.publish(relayMessage);

        log.info("▶ [Redis Pub 전파 완료] Target User: {}", userId);
    }
}
