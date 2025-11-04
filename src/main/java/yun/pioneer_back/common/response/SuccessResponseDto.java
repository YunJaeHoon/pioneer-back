package yun.pioneer_back.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuccessResponseDto
{
    private String message;
    private Object data;
}
