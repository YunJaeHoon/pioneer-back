package yun.pioneer_back.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SendVerificationCodeReqDto
{
    @Email
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
}
