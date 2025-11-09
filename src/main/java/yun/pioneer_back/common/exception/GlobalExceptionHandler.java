package yun.pioneer_back.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import yun.pioneer_back.common.response.ExceptionResponseDto;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{
    // 커스텀 예외 처리 핸들러
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponseDto> customExceptionHandler(CustomException e)
    {
        logErrorMessage(e, e.getErrorCode().name(), e.getErrorCode().getMessage(), e.getData());

        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ExceptionResponseDto.builder()
                        .code(e.getErrorCode().name())
                        .message(e.getErrorCode().getMessage())
                        .data(e.getData())
                        .build());
    }

    // 메서드 인자 유효성 검사 예외 처리 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("요청 값이 올바르지 않습니다.");

        logErrorMessage(e, CustomExceptionCode.INVALID_METHOD_ARGUMENT.name(), message, null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponseDto.builder()
                        .code(CustomExceptionCode.INVALID_METHOD_ARGUMENT.name())
                        .message(message)
                        .data(null)
                        .build());
    }

    // @PathVariable 유효성 검사 예외 처리 핸들러
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponseDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e)
    {
        logErrorMessage(e, CustomExceptionCode.INVALID_METHOD_ARGUMENT_TYPE.name(), e.getMessage(), null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponseDto.builder()
                        .code(CustomExceptionCode.INVALID_METHOD_ARGUMENT_TYPE.name())
                        .message(e.getMessage())
                        .data(null)
                        .build());
    }

    // 요청 dto 역직렬화 예외 처리 핸들러
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponseDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
    {
        logErrorMessage(e, CustomExceptionCode.INVALID_REQUEST_DTO.name(), e.getMessage(), null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponseDto.builder()
                        .code(CustomExceptionCode.INVALID_REQUEST_DTO.name())
                        .message(e.getMessage())
                        .data(null)
                        .build());
    }

    // 에러 메시지 로깅
    private void logErrorMessage(Exception e, String code, String message, Object data)
    {
        log.error("[{}]", e.getClass().getName());
        log.error("Code: {}", code);
        log.error("Message: {}", message);
        log.error("Data: {}", data);
    }
}
