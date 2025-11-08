package yun.pioneer_back.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import yun.pioneer_back.common.exception.CustomException;
import yun.pioneer_back.common.exception.CustomExceptionCode;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Service
public class TokenService
{
    private final Key key;

    public TokenService(@Value("${JWT_SIGNATURE_SECRET_KEY}") String secret)
    {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성
    public String createToken(TokenType tokenType, Map<String, Object> claimsMap)
    {
        Claims claims = Jwts.claims();

        // 사용자 정의 클레임 추가
        if (claimsMap != null) {
            claims.putAll(claimsMap);
        }

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expiredAt = now.plusSeconds(tokenType.getTtl());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(expiredAt.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
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

    // 사용자 정의 클레임 추출
    public Object getClaims(String token, String claimsKey, Class<?> requiredType)
    {
        // 클레임 추출
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

        if(claims.containsKey(claimsKey)) {
            return claims.get(claimsKey, requiredType);
        } else {
            throw new CustomException(CustomExceptionCode.CLAIMS_NOT_FOUND, claimsKey);
        }
    }
}
