package com.example.websocket.configuration.handler.client;

import java.lang.reflect.Type;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import com.example.websocket.biz.dto.CallMessage;
import com.example.websocket.biz.dto.UserInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * 다른 서버로 웹소켓 연결시 필요한 클래스
 */
@Slf4j
public class CustomStompSessionHandler extends StompSessionHandlerAdapter {

    private final String myServerId = "SERVER_BOT_01"; // 이 백엔드 서버의 식별자

    /**
     * 1. 다른 서버와 웹소켓/STOMP 연결이 완결되었을 때 호출
     */
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        log.info("✔ 타깃 웹소켓 서버 연결 성공! [Session ID: {}]", session.getSessionId());

        // [Step 1] 상대 서버로 사용자/서버 정보 등록 (/pub/user/init)
        UserInfo initData = new UserInfo(myServerId, "중계 백엔드 서버", "SERVER");
        session.send("/pub/user/init", initData);
        log.info("▶ 초기화 정보 송신: {}", myServerId);

        // [Step 2] 상대 서버의 특정 채널 구독 (/sub/call/{myServerId})
        session.subscribe("/sub/call/" + myServerId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return CallMessage.class; // 수신할 데이터의 DTO 타입
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                CallMessage message = (CallMessage) payload;
                log.info("🔔 [타깃 서버로부터 메시지 수신] Sender: {}, Type: {}, Msg: {}",
                        message.callerId(), message.type(), message.message());
            }
        });

        log.info("📡 개인 채널 구독 완료: /sub/call/{}", myServerId);
    }

    /**
     * 2. STOMP 에러 프레임 수신 시 호출
     */
    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        log.error("❌ STOMP 에러 프레임 수신: {}", payload);
    }

    /**
     * 3. 통신 중 예외 발생 시 호출
     */
    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
            Throwable exception) {
        log.error("❌ STOMP 통신 예외 발생", exception);
    }

    /**
     * 4. 전송/수신 객체 타입 매핑 지정
     */
    @Override
    public Type getPayloadType(StompHeaders headers) {
        return CallMessage.class;
    }
}
