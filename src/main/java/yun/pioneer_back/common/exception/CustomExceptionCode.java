package yun.pioneer_back.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CustomExceptionCode
{
    // 예시
    EXAMPLE_ERROR_CODE(HttpStatus.NOT_FOUND, "에러 코드 예시입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
