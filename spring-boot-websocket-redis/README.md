Key Point 요약
WebSocketStompClient: Spring에서 백엔드가 클라이언트 역할을 수행할 때 사용하는 핵심 클래스입니다.

StompSessionHandlerAdapter: 연결 후 실행할 초기화(init), 채널 구독(subscribe), 수신 메시지 디코딩 방식을 정의합니다.

SockJS 지원 여부:

타깃 서버가 .withSockJS() 설정이 되어 있다면: SockJsClient 객체를 래핑하여 WebSocketStompClient에 전달해야 합니다.

타깃 서버가 순수 WebSocket 엔드포인트라면: StandardWebSocketClient를 그대로 사용합니다.
