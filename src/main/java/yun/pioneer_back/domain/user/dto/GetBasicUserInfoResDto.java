package yun.pioneer_back.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetBasicUserInfoResDto
{
    private String nickname;    // 닉네임
    private int level;          // 레벨
    private int exp;            // 현재 경험치
    private int requiredExp;    // 레벨업에 필요한 총 경험치
}
