package com.example.websocket.biz.dto;

/**
 * 이중화 서버 간 레디스 Pub/Sub 메시지 전파용 DTO
 *
 * @param targetServerId 실제 소켓 세션을 가지고 있는 대상 서버 ID
 * @param targetSystemId 메시지를 최종 수신할 타깃 시스템 ID
 * @param message        전송할 메시지 본문
 */
public record OutboundRelayDto(
        String targetServerId,
        String targetSystemId,
        CallMessage message) {
}
