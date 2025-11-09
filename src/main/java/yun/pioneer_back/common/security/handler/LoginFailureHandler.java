package yun.pioneer_back.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import yun.pioneer_back.common.exception.CustomExceptionCode;
import yun.pioneer_back.common.response.ExceptionResponseDto;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler
{
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException
    {
        ExceptionResponseDto resDto = ExceptionResponseDto.builder()
                .code(CustomExceptionCode.LOGIN_FAILED.name())
                .message(CustomExceptionCode.LOGIN_FAILED.getMessage())
                .data(exception.getMessage())
                .build();

        response.setStatus(CustomExceptionCode.LOGIN_FAILED.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(resDto));
        response.getWriter().flush();
    }
}
