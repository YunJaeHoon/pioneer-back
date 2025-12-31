package yun.pioneer_back.common.entity.rdbms;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FightStatus
{
    MATCHED("매칭"),
    CANCELED("취소"),
    FINISHED("종료");

    private final String description;
}
