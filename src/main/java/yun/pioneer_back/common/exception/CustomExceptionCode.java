package yun.pioneer_back.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CustomExceptionCode
{
    // 예시
    EXAMPLE_ERROR_CODE(HttpStatus.NOT_FOUND, "에러 코드 예시입니다."),

    // 유효성 검사
    INVALID_METHOD_ARGUMENT(HttpStatus.BAD_REQUEST, "올바르지 않은 메서드 인자입니다."),
    INVALID_METHOD_ARGUMENT_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 메서드 인자 타입입니다."),
    INVALID_REQUEST_DTO(HttpStatus.BAD_REQUEST, "올바르지 않은 요청 dto입니다."),

    // 이메일
    SEND_EMAIL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송 중 에러가 발생하였습니다."),

    // Redis
    REDIS_OPERATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 작업 수행 중 에러가 발생하였습니다."),

    // 기타
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 예기치 못한 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
