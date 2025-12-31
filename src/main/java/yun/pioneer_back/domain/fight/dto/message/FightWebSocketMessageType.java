package yun.pioneer_back.domain.fight.dto.message;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FightWebSocketMessageType
{
    MATCH_FOUND("매칭 성공");

    private final String description;
}
