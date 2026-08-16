package com.example.websocket.biz.controller.client;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.websocket.biz.service.client.ExternalWebSocketClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/client-test")
@RequiredArgsConstructor
public class ClientTestController {

    private final ExternalWebSocketClientService clientService;

    // GET http://localhost:8080/api/client-test/send?targetId=userA
    @GetMapping("/send")
    public String sendToExternal(@RequestParam String targetId) {
        clientService.sendCallConnectToTarget(targetId);
        return "외부 웹소켓 서버로 [" + targetId + "] 대상 메시지 발송 요청함";
    }
}
