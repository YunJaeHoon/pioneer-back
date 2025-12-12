package yun.pioneer_back.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateNicknameReqDto
{
    @Size(min = 2, max = 12, message = "닉네임은 2글자 이상, 12글자 이하입니다.")
    private String nickname;
}
