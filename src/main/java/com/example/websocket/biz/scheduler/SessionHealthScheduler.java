package com.example.websocket.biz.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.websocket.biz.dto.dual.UserSessionStatus;
import com.example.websocket.configuration.manager.DualSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionHealthScheduler {

    private final DualSessionManager sessionManager;

    /**
     * 30초마다 주기적으로 전체 웹소켓 연결 상태 점검 (30,000ms)
     */
    @Scheduled(fixedRate = 30000)
    public void monitorSessionHealth() {
        List<UserSessionStatus> statuses = sessionManager.getAllSessionStatuses();

        if (statuses.isEmpty()) {
            log.debug("🔍 [연결 점검] 현재 관리 중인 웹소켓 세션이 없습니다.");
            return;
        }

        log.info("🔍 [주기적 세션 점검] 활성 사용자 총 {}명 점검 시작", statuses.size());

        for (UserSessionStatus status : statuses) {
            switch (status.statusSummary()) {
            case "BOTH_CONNECTED" ->
                log.info("✅ [정상 연결] User: {} (앱 🟢 | TM 🟢)", status.userId());
            case "APP_ONLY"       ->
                log.warn("⚠️ [부분 연결] User: {} (앱 🟢 | TM X)", status.userId());
            case "TM_ONLY"        ->
                log.warn("⚠️ [부분 연결] User: {} (앱 X | TM 🟢)", status.userId());
            default               ->
                log.warn("❌ [연결 끊김] User: {} (앱 X | TM X)", status.userId());
            }
        }
    }
}
