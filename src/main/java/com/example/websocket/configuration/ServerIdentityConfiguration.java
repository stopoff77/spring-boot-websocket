package com.example.websocket.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ServerIdentityConfiguration {

    @Bean(name = "myServerId")
    String myServerId(@Value("${server.id:SERVER_A}") String myServerId) {
        log.debug("myServerId {}", myServerId);
        try {
            // [IP + UUID 일부] 조합으로 고유 서버 ID 생성 (예: 192.168.1.15-a1b2c3d4)
            String ip        = InetAddress.getLocalHost().getHostAddress();
            String shortUuid = UUID.randomUUID().toString().substring(0, 8);

            log.debug("myServerId {}", ip + "-" + shortUuid);

            return ip + "-" + shortUuid;
        } catch (UnknownHostException e) {
            // 예외 발생 시 랜던 UUID 사용
            return "SERVER-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
}
