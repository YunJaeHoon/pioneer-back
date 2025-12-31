package yun.pioneer_back.common.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import yun.pioneer_back.common.entity.rdbms.User;
import yun.pioneer_back.common.repository.rdbms.UserRepository;
import yun.pioneer_back.common.security.CustomUserDetailsService;
import yun.pioneer_back.common.security.handler.LoginSuccessHandler;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter
{
    private final TokenService tokenService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        String authorizationHeader = request.getHeader("Authorization");

        // 헤더에 토큰이 존재하는지 체크
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
        {
            String accessToken = authorizationHeader.substring(7);

            // 토큰이 유효한지 체크
            if(tokenService.checkToken(accessToken))
            {
                // access token 페이로드 추출
                LoginSuccessHandler.AuthenticationTokenPayload authenticationTokenPayload = tokenService.getPayload(
                        accessToken,
                        LoginSuccessHandler.AuthenticationTokenPayload.class
                );

                // access token 페이로드 내의 유저 ID 추출
                Long userId = authenticationTokenPayload.userId();

                // 유저 조회
                Optional<User> userOptional = userRepository.findById(userId);

                // 존재하는 계정인지 체크
                if(userOptional.isPresent())
                {
                    User user = userOptional.get();
                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

                    if(userDetails != null)
                    {
                        // 접근 권한 인증 토큰 생성
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        // 현재 요청의 security context에 접근 권한 부여
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            }
        }

        // 다음 필터로 전달
        filterChain.doFilter(request, response);
    }
}
