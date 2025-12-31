package yun.pioneer_back.common.security.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Service
public class TokenService
{
    private final Key key;
    private final ObjectMapper objectMapper;

    public TokenService(@Value("${JWT_SIGNATURE_SECRET_KEY}") String secret, ObjectMapper objectMapper)
    {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.objectMapper = objectMapper;
    }

    // 토큰 생성
    public <T extends TokenPayload> String createToken(
            TokenType tokenType,
            T payload
    ) {
        Claims claims = Jwts.claims();

        // 페이로드 포함
        if (payload != null) {
            Map<String, Object> payloadMap = objectMapper.convertValue(payload, new TypeReference<>() {});
            claims.putAll(payloadMap);
        }

        // 만료 시간 설정
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expiredAt = now.plusSeconds(tokenType.getTtl());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(expiredAt.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰으로부터 페이로드 추출
    public <T extends TokenPayload> T getPayload(
            String token,
            Class<T> payloadClass
    ) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return objectMapper.convertValue(claims, payloadClass);
    }


    // 토큰을 쿠키로 변환
    public Cookie parseTokenToCookie(String token, TokenType tokenType)
    {
        Cookie cookie = new Cookie(tokenType.getName(), token);

        cookie.setHttpOnly(tokenType.isHttpOnly());
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(tokenType.getTtl());
        cookie.setAttribute("SameSite", "Strict");

        return cookie;
    }

    // 토큰 검증
    public boolean checkToken(String token)
    {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 만료된 토큰 쿠키 생성
    public Cookie createExpiredCookie(TokenType tokenType)
    {
        Cookie cookie = new Cookie(tokenType.getName(), "");

        cookie.setHttpOnly(tokenType.isHttpOnly());
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");

        return cookie;
    }
}
