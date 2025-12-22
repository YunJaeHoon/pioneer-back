package yun.pioneer_back.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yun.pioneer_back.common.entity.rdbms.User;
import yun.pioneer_back.common.repository.rdbms.UserRepository;
import yun.pioneer_back.common.response.SuccessResponseDto;
import yun.pioneer_back.common.security.CustomUserDetails;
import yun.pioneer_back.common.security.jwt.TokenService;
import yun.pioneer_back.common.security.jwt.TokenType;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler
{
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException
    {
        // 계정 정보
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // access token 및 refresh token 발급
        String accessToken = tokenService.createToken(TokenType.ACCESS_TOKEN, Map.of("userId", user.getId()));
        String refreshToken = tokenService.createToken(TokenType.REFRESH_TOKEN, Map.of("userId", user.getId()));

        // 사용자 refresh token 정보 입력
        user.renewRefreshToken(refreshToken);
        userRepository.save(user);

        // 토큰을 쿠키로 변환
        Cookie accessTokenCookie = tokenService.parseTokenToCookie(accessToken, TokenType.ACCESS_TOKEN);
        Cookie refreshTokenCookie = tokenService.parseTokenToCookie(refreshToken, TokenType.REFRESH_TOKEN);

        // 쿠키를 응답에 포함
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        SuccessResponseDto responseDto = SuccessResponseDto.builder()
                .message("로그인에 성공하였습니다.")
                .data(null)
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        response.getWriter().flush();
    }
}
