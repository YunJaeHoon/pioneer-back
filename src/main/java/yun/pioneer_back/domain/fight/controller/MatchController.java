package yun.pioneer_back.domain.fight.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yun.pioneer_back.common.entity.rdbms.User;
import yun.pioneer_back.common.response.SuccessResponseDto;
import yun.pioneer_back.common.security.CustomUserDetails;
import yun.pioneer_back.domain.fight.service.MatchService;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController
{
    private final MatchService matchService;

    // 랜덤 매칭 요청
    @PostMapping("/request")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> request(@AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        matchService.request(user);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("랜덤 매칭을 성공적으로 요청하였습니다.")
                        .data(null)
                        .build());
    }
}
