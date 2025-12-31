package yun.pioneer_back.common.configuration.web_socket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer
{
    private final CustomChannelInterceptor channelInterceptor;

    // WebSocket 연결 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry)
    {
        stompEndpointRegistry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // WebSocket 메시지 엔드포인트 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry)
    {
        // 발행 엔드포인트
        messageBrokerRegistry.setApplicationDestinationPrefixes("/pub");

        // 구독 엔드포인트
        messageBrokerRegistry.enableSimpleBroker("/sub");

        // 개인 메시지 엔드포인트
        messageBrokerRegistry.setUserDestinationPrefix("/user");
    }

    // 커스텀 STOMP 인터셉터 설정
    @Override public void configureClientInboundChannel(ChannelRegistration channelRegistration) {
        channelRegistration.interceptors(channelInterceptor);
    }
}
