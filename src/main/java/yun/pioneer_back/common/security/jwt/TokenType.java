package yun.pioneer_back.common.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TokenType
{
    EMAIL_VERIFICATION_TOKEN("email-verification-token", 60 * 60, true),
    ACCESS_TOKEN("access-token", 60 * 60, false),
    REFRESH_TOKEN("refresh-token", 60 * 60 * 24 * 30, true);

    private final String name;
    private final int ttl;
    private final boolean isHttpOnly;
}
