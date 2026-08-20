package com.example.websocket.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.websocket.configuration.websocket.handler.RedisStompHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

//    private final StompHandler     stompHandler;    // 1. 주입 받기
//    private final DualStompHandler dualStompHandler;
    private final RedisStompHandler redisStompHandler;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트에서 최초 WebSocket 연결을 맺을 엔드포인트 (ws://localhost:8080/ws-stomp)
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*") // CORS 허용 (실운영 환경에서는 specific origin 지정 권장)
                .withSockJS(); // SockJS fallback 옵션 지원

        // 다른 web app에서 연결 맺을 앤드 포인트
        registry.addEndpoint("/ws-for-tm")
                .setAllowedOriginPatterns("*") // CORS 허용 (실운영 환경에서는 specific origin 지정 권장)
                .withSockJS(); // SockJS fallback 옵션 지원

        // 앱과 맺을 앤드포인트
        registry.addEndpoint("/ws-for-app")
                .setAllowedOriginPatterns("*") // CORS 허용 (실운영 환경에서는 specific origin 지정 권장)
                .withSockJS(); // SockJS fallback 옵션 지원

        // Native App 전용 Pure WebSocket 엔드포인트 예시
        registry.addEndpoint("/ws-for-app-native")
                .setAllowedOriginPatterns("*"); // .withSockJS() 제외
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 구독(Subscribe) 요청 Prefix -> 클라이언트가 메시지를 받을 때 사용하는 채널 경로
        registry.enableSimpleBroker("/sub");

        // 메시지 발행(Publish) 요청 Prefix -> 클라이언트가 서버로 메시지를 보낼 때 요청하는 경로
        registry.setApplicationDestinationPrefixes("/pub");
    }


    // ★ 2. 이 메서드가 빠져있다면 StompHandler가 실행되지 않습니다!
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(stompHandler);
//        registration.interceptors(dualStompHandler);
        registration.interceptors(redisStompHandler);
    }
}
