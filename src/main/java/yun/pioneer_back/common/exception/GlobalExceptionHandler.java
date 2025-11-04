package yun.pioneer_back.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yun.pioneer_back.common.response.ExceptionResponseDto;

import java.util.Arrays;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{
    // 커스텀 예외 처리 핸들러
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponseDto> customExceptionHandler(CustomException e)
    {
        log.error("[CustomException] code: {} | message: {} | data: {}",
                e.getErrorCode().name(),
                e.getErrorCode().getMessage(),
                e.getData());

        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ExceptionResponseDto.builder()
                        .code(e.getErrorCode().name())
                        .message(e.getErrorCode().getMessage())
                        .data(e.getData())
                        .build());
    }

    // 그 외 모든 예외 처리 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> globalExceptionHandler(Exception e)
    {
        log.error("[UnhandledException] {}", e.getClass().getName(), e);
        log.error("Message: {}", e.getMessage());
        log.error("StackTrace: {}", Arrays.toString(e.getStackTrace()));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponseDto.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message("서버 내부에서 예기치 못한 오류가 발생했습니다.")
                        .data(null)
                        .build());
    }
}
