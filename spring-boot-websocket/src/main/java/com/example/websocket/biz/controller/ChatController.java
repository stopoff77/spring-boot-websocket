package com.example.websocket.biz.controller;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.example.websocket.biz.dto.ChatMessage;
import com.example.websocket.util.Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;


    /**
     * 방법 1: SimpMessageSendingOperations 사용 (동적 라우팅 가능)
     * 클라이언트가 `/pub/chat/message`로 송신할 때 동작
     */
    @MessageMapping("/chat/message")
    public void receiveMessage(ChatMessage message) {

        log.debug("message\n{}", message.message());
        log.debug("message\n{}\n", Util.toString(message));

        // 입장 처리
        if (ChatMessage.MessageType.ENTER.equals(message.type())) {
            ChatMessage enterMessage = new ChatMessage(
                    ChatMessage.MessageType.ENTER,
                    message.roomId(),
                    "SYSTEM",
                    message.sender() + "님이 입장하셨습니다.");
            // /sub/chat/room/{roomId} 채널 구독자 전체에게 전송
            messagingTemplate.convertAndSend("/sub/chat/room/" + message.roomId(), enterMessage);
            return;
        }

        // 일반 채팅 메시지 브로드캐스팅
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.roomId(), message);
    }

    /**
     * 방법 2: @SendTo 어노테이션 사용 (정적 라우팅)
     * 클라이언트가 `/pub/chat/broadcast`로 송신 시, 리턴값이 `/sub/public` 구독자들에게 자동 전송
     */
    @MessageMapping("/chat/broadcast")
    @SendTo("/sub/public")
    public ChatMessage broadcastMessage(ChatMessage message) {
        return message;
    }
}
