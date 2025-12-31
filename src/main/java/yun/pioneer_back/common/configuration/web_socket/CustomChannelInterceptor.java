package yun.pioneer_back.common.configuration.web_socket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import yun.pioneer_back.common.exception.CustomException;
import yun.pioneer_back.common.exception.CustomExceptionCode;
import yun.pioneer_back.common.security.handler.LoginSuccessHandler;
import yun.pioneer_back.common.security.jwt.TokenService;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomChannelInterceptor implements ChannelInterceptor
{
    private final TokenService tokenService;

    // 서버로 들어오는 STOMP 메시지 처리
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel)
    {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(accessor != null)
        {
            log.info("[WebSocket INTERCEPT] cmd={}, headers={}", accessor.getCommand(), accessor.toNativeHeaderMap());

            if(StompCommand.CONNECT.equals(accessor.getCommand()))
            {
                String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

                if(authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
                {
                    String accessToken = authorizationHeader.substring(7);

                    if(tokenService.checkToken(accessToken))
                    {
                        // access token 페이로드 추출
                        LoginSuccessHandler.AuthenticationTokenPayload authenticationTokenPayload = tokenService.getPayload(
                                accessToken,
                                LoginSuccessHandler.AuthenticationTokenPayload.class
                        );

                        // access token 페이로드 내의 유저 ID 추출
                        Long userId = authenticationTokenPayload.userId();

                        // 유저 설정
                        accessor.setUser(userId::toString);

                        return message;
                    }
                }
            }
        } else {
            throw new CustomException(CustomExceptionCode.ACCESSOR_NOT_FOUND, null);
        }

        return message;
    }
}