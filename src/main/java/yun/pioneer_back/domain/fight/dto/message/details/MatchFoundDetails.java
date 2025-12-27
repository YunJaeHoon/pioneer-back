package yun.pioneer_back.domain.fight.dto.message.details;

import lombok.Builder;
import yun.pioneer_back.domain.fight.dto.message.FightWebSocketMessageDetails;

@Builder
public class MatchFoundDetails extends FightWebSocketMessageDetails
{
    private String roomId;              // 방 UUID
    private Long myUserId;              // 내 유저 ID
    private String myNickname;          // 내 닉네임
    private Long opponentUserId;        // 상대방 유저 ID
    private String opponentNickname;    // 상대방 닉네임
}
