package yun.pioneer_back.common.configuration.web_socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
public class WebSocketEventHandler
{
    // WebSocket 연결 이벤트 처리
    @EventListener
    public void onConnect(SessionConnectedEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        log.info("[WebSocket:CONNECT] user={}, nativeHeaders={}", accessor.getUser() == null ? "null" : accessor.getUser().getName(), accessor.toNativeHeaderMap());
    }

    // 구독 이벤트 처리
    @EventListener
    public void onSubscribe(SessionSubscribeEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        log.info("[WebSocket:SUBSCRIBE] dest={}, user={}", accessor.getDestination(), accessor.getUser() == null ? "null" : accessor.getUser().getName());
    }
}
