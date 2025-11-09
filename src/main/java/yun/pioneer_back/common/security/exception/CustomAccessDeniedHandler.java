package yun.pioneer_back.common.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import yun.pioneer_back.common.exception.CustomExceptionCode;
import yun.pioneer_back.common.response.ExceptionResponseDto;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler
{
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException
    {
        ExceptionResponseDto resDto = ExceptionResponseDto.builder()
                .code(CustomExceptionCode.LOW_AUTHORITY.name())
                .message(CustomExceptionCode.LOW_AUTHORITY.getMessage())
                .data(null)
                .build();

        response.setStatus(CustomExceptionCode.LOW_AUTHORITY.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(resDto));
        response.getWriter().flush();
    }
}
