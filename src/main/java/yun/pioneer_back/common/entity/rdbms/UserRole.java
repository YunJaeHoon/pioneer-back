package yun.pioneer_back.common.entity.rdbms;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserRole
{
    ADMIN("관리자 권한"),
    USER("일반 사용자 권한");

    private final String description;
}
