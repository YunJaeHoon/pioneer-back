package yun.pioneer_back.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yun.pioneer_back.common.util.EmailUtil;
import yun.pioneer_back.common.util.RedisUtil;
import yun.pioneer_back.domain.user.dto.SendVerificationCodeReqDto;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final EmailUtil emailUtil;
    private final RedisUtil redisUtil;

    // 이메일 인증번호 전송
    @Transactional
    public void sendVerificationCode(SendVerificationCodeReqDto reqDto)
    {
        // 무작위 인증번호 생성
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(1_0000_0000);
        String verificationCode = String.format("%08d", number);

        // Redis에 저장
        redisUtil.set("email:verification_code:" + reqDto.getEmail(), verificationCode, Duration.ofMinutes(10));

        // 이메일 전송
        emailUtil.sendEmail(
                reqDto.getEmail(),
                "[서부의 바람은, 손끝으로 분다] 이메일 인증번호",
                String.format(
                        """
                            <div style="display: flex; flex-direction: column; align-items: center; margin: 20px;">
                                <div style="width: 100%%; font-size: 1.125rem; font-weight: 400; color: #373737; margin-top: 50px;">
                                    다음 인증번호를 <b>인증번호 확인란</b>에 입력하시게. <br />
                                    인증번호가 틀리면 다시 인증번호가 전송되니 주의하도록!
                                </div>
                                <div style="font-size: 2.5rem; font-weight: 600; color: #373737; margin-top: 100px; margin-bottom: 100px;">%s</div>
                            </div>
                        """,
                        verificationCode
                ));
    }
}
