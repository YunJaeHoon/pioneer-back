package yun.pioneer_back.common.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class FightMatchConfig implements WebSocketMessageBrokerConfigurer
{
    // WebSocket 연결 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry)
    {
        stompEndpointRegistry.addEndpoint("/fight")
                .withSockJS();
    }

    // WebSocket 메시지 발행/구독 엔드포인트 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry)
    {
        messageBrokerRegistry.setApplicationDestinationPrefixes("/pub");
        messageBrokerRegistry.enableSimpleBroker("/sub");
    }
}
