package yun.pioneer_back.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException
{
    private CustomExceptionCode errorCode;
    private Object data;
}
