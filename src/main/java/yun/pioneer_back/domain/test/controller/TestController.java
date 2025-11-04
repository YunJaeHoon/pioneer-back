package yun.pioneer_back.domain.test.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ExceptionResponseDto> testError()
    {
        throw new CustomException(CustomExceptionCode.EXAMPLE_ERROR_CODE, null);
    }
}
