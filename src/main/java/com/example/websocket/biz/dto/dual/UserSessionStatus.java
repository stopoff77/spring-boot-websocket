package com.example.websocket.biz.dto.dual;

public record UserSessionStatus(
        String userId,
        boolean isAppConnected,
        boolean isTmConnected,
        String statusSummary // BOTH_CONNECTED, APP_ONLY, TM_ONLY, BOTH_DISCONNECTED
) {
    public static UserSessionStatus of(String userId, boolean appConnected, boolean tmConnected) {
        String summary;
        if (appConnected && tmConnected) {
            summary = "BOTH_CONNECTED";
        } else if (appConnected) {
            summary = "APP_ONLY";
        } else if (tmConnected) {
            summary = "TM_ONLY";
        } else {
            summary = "BOTH_DISCONNECTED";
        }
        return new UserSessionStatus(userId, appConnected, tmConnected, summary);
    }
}
