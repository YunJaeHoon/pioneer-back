package yun.pioneer_back.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateProfileImageReqDto
{
    @NotBlank
    private String profileImage;
}
