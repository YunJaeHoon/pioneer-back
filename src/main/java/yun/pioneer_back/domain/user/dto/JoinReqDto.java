package yun.pioneer_back.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class JoinReqDto
{
    @Email
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @Size(min = 8, max = 20, message = "비밀번호는 8글자 이상, 20글자 이하입니다.")
    private String password;

    @Size(min = 2, max = 12, message = "닉네임은 2글자 이상, 12글자 이하입니다.")
    private String nickname;
}
