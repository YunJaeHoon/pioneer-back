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

    // User 관련
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "로그인에 실패하였습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 비밀번호 형식입니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 닉네임 형식입니다."),
    UNAUTHORIZED_EMAIL(HttpStatus.UNAUTHORIZED, "인증받지 않은 이메일입니다."),
    NOT_LOGIN(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다"),
    LOW_AUTHORITY(HttpStatus.FORBIDDEN, "권한이 부족합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자가 존재하지 않습니다."),
    CLAIMS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자 정의 클레임 값이 존재하지 않습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.CONFLICT, "이미 만료된 인증번호 데이터입니다."),
    WRONG_VERIFICATION_CODE(HttpStatus.CONFLICT, "틀린 인증번호입니다."),
    ALREADY_USED_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    ALREADY_USED_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    // 기타
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 예기치 못한 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
