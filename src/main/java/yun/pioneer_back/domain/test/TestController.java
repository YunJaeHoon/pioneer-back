package yun.pioneer_back.domain.test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yun.pioneer_back.common.exception.CustomException;
import yun.pioneer_back.common.exception.CustomExceptionCode;
import yun.pioneer_back.common.response.ExceptionResponseDto;
import yun.pioneer_back.common.response.SuccessResponseDto;

@RestController
@RequestMapping("/api/test")
public class TestController
{
    // API 성공 응답 테스트
    @GetMapping("/success")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> testSuccess()
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("API 성공 응답입니다.")
                        .data(null)
                        .build());
    }

    // API 에러 응답 테스트
    @GetMapping("/error")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ExceptionResponseDto> testError()
    {
        throw new CustomException(CustomExceptionCode.EXAMPLE_ERROR_CODE, null);
    }

    // USER 권한 테스트
    @GetMapping("/role/user")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> testRoleUser()
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("USER 권한을 가지고 있습니다.")
                        .data(null)
                        .build());
    }

    // ADMIN 권한 테스트
    @GetMapping("/role/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SuccessResponseDto> testRoleAdmin()
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("ADMIN 권한을 가지고 있습니다.")
                        .data(null)
                        .build());
    }
}
