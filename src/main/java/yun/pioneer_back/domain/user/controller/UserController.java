package yun.pioneer_back.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yun.pioneer_back.common.response.SuccessResponseDto;
import yun.pioneer_back.domain.user.dto.SendVerificationCodeReqDto;
import yun.pioneer_back.domain.user.service.UserService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    // 이메일 인증번호 전송
    @PostMapping("/send-verification-code")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> sendVerificationCode(@Valid @RequestBody SendVerificationCodeReqDto reqDto)
    {
        userService.sendVerificationCode(reqDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("이메일 인증번호를 성공적으로 전송하였습니다.")
                        .data(null)
                        .build());
    }
}
