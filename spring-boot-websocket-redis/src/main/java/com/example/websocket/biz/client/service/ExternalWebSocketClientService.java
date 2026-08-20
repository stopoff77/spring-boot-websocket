package com.example.websocket.biz.client.service;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.example.websocket.biz.call.dto.CallMessage;
import com.example.websocket.configuration.websocket.handler.client.CustomStompSessionHandler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExternalWebSocketClientService {

    private StompSession stompSession;

    // 타깃 서버 URL (SockJS 사용하는 경우 ws/wss 대신 http/https 엔드포인트 지정)
//    private final String TARGET_SERVER_URL = "ws://localhost:8080/ws-stomp";
    @Value("${websocket.target.url}")
    private String TARGET_SERVER_URL;


    /**
     * 서비스 기동 시 외부 웹소켓 서버로 연결 실행
     */
    @PostConstruct
    public void connectToExternalServer() {
        log.debug("TARGET_SERVER_URL {}", TARGET_SERVER_URL);


        // 1. 순수 WebSocket 클라이언트 생성 (SockJS가 적용된 서버라면 SockJsClient 조합)
        WebSocketClient webSocketClient = new StandardWebSocketClient();

        // 만약 타깃 서버가 SockJS를 사용 중이라면 아래 주석을 해제하고 SockJsClient 사용
        /*
         * List<Transport> transports = Collections.singletonList(new
         * WebSocketTransport(webSocketClient));
         * WebSocketClient sockJsClient = new SockJsClient(transports);
         * WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
         */

        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);

        // 2. JSON 메시지 변환기 설정 (Jackson)
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        // 3. 연결 요청
        try {
            CustomStompSessionHandler sessionHandler = new CustomStompSessionHandler();

            // 비동기 연결 (CompletableFuture 반환)
            this.stompSession = stompClient
                    .connectAsync(TARGET_SERVER_URL, sessionHandler)
                    .get(); // 연결 완결까지 대기

            log.info("외부 서버 연결 성공 여부: {}", stompSession.isConnected());

        } catch (InterruptedException | ExecutionException e) {
//            log.error("외부 웹소켓 서버 연결 실패", e);
            log.error("외부 웹소켓 서버 연결 실패 {}", e.toString());
        }
    }

    /**
     * 외부 서버로 통화 연결 메시지 송신 예제
     */
    public void sendCallConnectToTarget(String targetUserId) {
        if (stompSession != null && stompSession.isConnected()) {
            CallMessage payload = new CallMessage(
                    "SERVER_BOT_01",
                    targetUserId,
                    "CONNECT",
                    "백엔드 서버에서 발송한 통화 연결 요청입니다.");

            // 타깃 서버의 /pub/call/connect 경로로 메시지 전송
            stompSession.send("/pub/call/connect", payload);
            log.info("▶ 타깃 서버로 메시지 발송 완료: {}", targetUserId);
        } else {
            log.warn("⚠️ 웹소켓 세션이 연결되어 있지 않습니다.");
        }
    }
}
