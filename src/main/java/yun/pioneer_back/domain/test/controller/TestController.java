package yun.pioneer_back.domain.test.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponseDto.builder()
                        .code("NOT_FOUND")
                        .message("API 에러 응답입니다.")
                        .data(null)
                        .build());
    }
}
