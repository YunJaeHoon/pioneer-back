package yun.pioneer_back.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yun.pioneer_back.common.entity.User;
import yun.pioneer_back.common.exception.CustomException;
import yun.pioneer_back.common.exception.CustomExceptionCode;
import yun.pioneer_back.common.repository.UserRepository;
import yun.pioneer_back.common.entity.UserRole;
import yun.pioneer_back.common.security.jwt.TokenService;
import yun.pioneer_back.common.security.jwt.TokenType;
import yun.pioneer_back.common.util.EmailUtil;
import yun.pioneer_back.common.util.RedisUtil;
import yun.pioneer_back.domain.user.dto.CheckVerificationCodeReqDto;
import yun.pioneer_back.domain.user.dto.JoinReqDto;
import yun.pioneer_back.domain.user.dto.SendVerificationCodeReqDto;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;

    private final EmailUtil emailUtil;
    private final RedisUtil redisUtil;

    private final TokenService tokenService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 8~20 글자, (영문, 숫자, 특수문자)를 모두 포함
    private final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,20}$";

    // 2~12 글자, (영문, 한글, 숫자)만 허용
    private final String NICKNAME_REGEX = "^[A-Za-z0-9가-힣]{2,12}$";

    // 이메일 인증번호 전송
    @Transactional
    public void sendVerificationCode(SendVerificationCodeReqDto reqDto)
    {
        // 무작위 인증번호 생성
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(1_0000_0000);
        String verificationCode = String.format("%08d", number);

        // Redis에 <이메일, 인증번호> 데이터 저장
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
                                    인증번호가 틀리면 인증번호를 다시 전송해야 하니 주의하도록!
                                </div>
                                <div style="font-size: 2.5rem; font-weight: 600; color: #373737; margin-top: 100px; margin-bottom: 100px;">%s</div>
                            </div>
                        """,
                        verificationCode
                ));
    }

    // 이메일 인증번호 확인
    @Transactional
    public void checkVerificationCode(CheckVerificationCodeReqDto reqDto, HttpServletResponse response)
    {
        // Redis에서 인증번호 조회
        Object verificationCodeValue = redisUtil.get("email:verification_code:" + reqDto.getEmail());

        // 인증번호 데이터가 존재하지 않는다면, 인증번호 만료 예외 처리
        if(verificationCodeValue == null) {
            throw new CustomException(CustomExceptionCode.EXPIRED_VERIFICATION_CODE, null);
        }

        // 올바른 인증번호
        String verificationCode = (String) verificationCodeValue;

        // 인증번호가 틀린 경우
        if(!verificationCode.equals(reqDto.getVerificationCode()))
        {
            // 인증번호 데이터 삭제
            redisUtil.delete("email:verification_code:" + reqDto.getEmail());

            // 예외 처리
            throw new CustomException(CustomExceptionCode.WRONG_VERIFICATION_CODE, null);
        }

        // 이메일 인증 토큰 발급
        String verificationToken = tokenService.createToken(
                TokenType.EMAIL_VERIFICATION_TOKEN,
                Map.of("email", reqDto.getEmail())
        );

        // 토큰을 쿠키로 변환
        Cookie verificationCookie = tokenService.parseTokenToCookie(verificationToken, TokenType.EMAIL_VERIFICATION_TOKEN);

        // 쿠키를 응답에 포함
        response.addCookie(verificationCookie);
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public void checkNicknameDuplication(String nickname)
    {
        // 닉네임 중복 확인
        if(userRepository.findByNickname(nickname).isPresent()) {
            throw new CustomException(CustomExceptionCode.ALREADY_USED_NICKNAME, nickname);
        }
    }

    // 회원가입
    @Transactional
    public void join(JoinReqDto reqDto, String verificationToken)
    {
        // 이메일 인증 토큰 검증
        tokenService.checkToken(verificationToken);

        // 이메일 인증 토큰 내의 이메일 정보 추출
        String verifiedEmail = tokenService.getClaims(verificationToken, "email", String.class);

        // 이메일 인증 여부 확인
        if(!verifiedEmail.equals(reqDto.getEmail())) {
            throw new CustomException(CustomExceptionCode.UNAUTHORIZED_EMAIL, reqDto.getEmail());
        }

        // 비밀번호 형식 체크
        if(!Pattern.matches(PASSWORD_REGEX, reqDto.getPassword())) {
            throw new CustomException(CustomExceptionCode.INVALID_PASSWORD_FORMAT, reqDto.getPassword());
        }

        // 닉네임 형식 체크
        if(!Pattern.matches(NICKNAME_REGEX, reqDto.getNickname())) {
            throw new CustomException(CustomExceptionCode.INVALID_NICKNAME_FORMAT, reqDto.getNickname());
        }

        // 이메일 중복 확인
        if(userRepository.findByEmail(reqDto.getEmail()).isPresent()) {
            throw new CustomException(CustomExceptionCode.ALREADY_USED_EMAIL, reqDto.getEmail());
        }

        // 닉네임 중복 확인
        if(userRepository.findByNickname(reqDto.getNickname()).isPresent()) {
            throw new CustomException(CustomExceptionCode.ALREADY_USED_NICKNAME, reqDto.getNickname());
        }

        // 사용자 정보 생성
        User user = User.builder()
                .email(reqDto.getEmail())
                .password(bCryptPasswordEncoder.encode(reqDto.getPassword()))
                .nickname(reqDto.getNickname())
                .role(UserRole.USER)
                .refreshToken(null)
                .build();

        // 사용자 정보 저장
        userRepository.save(user);
    }
}
