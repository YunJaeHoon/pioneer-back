package yun.pioneer_back.domain.fight.dto.message;

import lombok.*;
import yun.pioneer_back.domain.fight.dto.message.details.MatchFoundDetails;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class FightWebSocketMessage
{
    private FightWebSocketMessageType type;
    private FightWebSocketMessageDetails details;

    // 매칭 성공 WebSocket 메시지 생성
    public static FightWebSocketMessage create(MatchFoundDetails details)
    {
        return FightWebSocketMessage.builder()
                .type(FightWebSocketMessageType.MATCH_FOUND)
                .details(details)
                .build();
    }
}
