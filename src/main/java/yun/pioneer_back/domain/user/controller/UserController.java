package yun.pioneer_back.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import yun.pioneer_back.common.response.SuccessResponseDto;
import yun.pioneer_back.domain.user.dto.CheckVerificationCodeReqDto;
import yun.pioneer_back.domain.user.dto.JoinReqDto;
import yun.pioneer_back.domain.user.dto.ResetPasswordReqDto;
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

    // 이메일 인증번호 확인
    @PostMapping("/check-verification-code")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> checkVerificationCode(@Valid @RequestBody CheckVerificationCodeReqDto reqDto,
                                                                    HttpServletResponse response)
    {
        userService.checkVerificationCode(reqDto, response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("이메일 인증번호를 성공적으로 확인하였습니다.")
                        .data(null)
                        .build());
    }

    // 닉네임 중복 확인
    @GetMapping("/check-nickname-duplication")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> checkNicknameDuplication(@Valid @RequestParam String nickname)
    {
        userService.checkNicknameDuplication(nickname);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("닉네임 중복 확인에 성공하였습니다.")
                        .data(null)
                        .build());
    }

    // 회원가입
    @PostMapping("/join")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> join(@Valid @RequestBody JoinReqDto reqDto,
                                                   @CookieValue(name = "email-verification-token") String verificationToken)
    {
        userService.join(reqDto, verificationToken);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("성공적으로 회원가입 되었습니다.")
                        .data(null)
                        .build());
    }

    // 비밀번호 초기화
    @PostMapping("/reset-password")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> resetPassword(@Valid @RequestBody ResetPasswordReqDto reqDto)
    {
        userService.resetPassword(reqDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("비밀번호를 성공적으로 초기화하였습니다.")
                        .data(null)
                        .build());
    }

    // access token 재발급
    @PostMapping("/refresh-access-token")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> refreshAccessToken(@CookieValue(name = "refresh-token") String refreshToken,
                                                                 HttpServletResponse response)
    {
        userService.refreshAccessToken(refreshToken, response);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("access token 재발급에 성공하였습니다.")
                        .data(null)
                        .build());
    }
}
