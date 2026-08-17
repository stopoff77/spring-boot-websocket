Key Point 요약
WebSocketStompClient: Spring에서 백엔드가 클라이언트 역할을 수행할 때 사용하는 핵심 클래스입니다.

StompSessionHandlerAdapter: 연결 후 실행할 초기화(init), 채널 구독(subscribe), 수신 메시지 디코딩 방식을 정의합니다.

SockJS 지원 여부:

타깃 서버가 .withSockJS() 설정이 되어 있다면: SockJsClient 객체를 래핑하여 WebSocketStompClient에 전달해야 합니다.

타깃 서버가 순수 WebSocket 엔드포인트라면: StandardWebSocketClient를 그대로 사용합니다.


[앱 (Client)] ──(WS)──► [WAS 1번] ──► 1. Redis 전역 세션 검증 (TM 연결 여부)
                                  │         2. Redis Topic으로 메시지 발행 (Publish)
                                  ▼
                    ┌──────────────────┐
                    │   Redis Server                     │ (Topic: websocket:relay:topic)
                    └────────┬─────────┘
                                      │ (Broadcasting)
             ┌───────────────┴───────────────┐
             ▼                                                              ▼
         [WAS 1번]                                                       [WAS 2번] (Redis Subscriber)
  (로컬 TM 세션 없음 -> 무시)                                    (로컬 TM 세션 보유 확인!)
                                                                            │
                                                                            └──(WS)──► [TM AP (Client)]
                                             
                                             