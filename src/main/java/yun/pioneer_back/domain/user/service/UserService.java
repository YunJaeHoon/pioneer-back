package yun.pioneer_back.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import yun.pioneer_back.common.util.LevelUtil;
import yun.pioneer_back.common.util.RedisUtil;
import yun.pioneer_back.domain.user.dto.*;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;

    private final EmailUtil emailUtil;
    private final RedisUtil redisUtil;
    private final LevelUtil levelUtil;

    private final TokenService tokenService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 8~20 글자, (영문, 숫자, 특수문자)를 모두 포함
    private final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,20}$";

    // 2~12 글자, (영문, 한글, 숫자)만 허용
    private final String NICKNAME_REGEX = "^[A-Za-z0-9가-힣]{2,12}$";

    // 비밀번호 허용 영문, 숫자, 특수문자
    private static final String PASSWORD_POSSIBLE_ENGLISH = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String PASSWORD_POSSIBLE_NUMBER = "0123456789";
    private static final String PASSWORD_POSSIBLE_SPECIAL = "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?";
    private static final String PASSWORD_ALL_POSSIBLE_LETTERS = PASSWORD_POSSIBLE_ENGLISH + PASSWORD_POSSIBLE_NUMBER + PASSWORD_POSSIBLE_SPECIAL;

    // 이메일 인증번호 전송
    @Transactional
    public void sendVerificationCode(SendVerificationCodeReqDto reqDto)
    {
        // 이메일 중복 확인
        if(userRepository.findByEmail(reqDto.getEmail()).isPresent())
        {
            // 이메일 전송
            emailUtil.sendEmail(
                    reqDto.getEmail(),
                    "[서부의 바람은, 손끝에서 분다] 이메일 인증번호",
                    """
                                <div style="display: flex; flex-direction: column; align-items: center; margin: 20px;">
                                    <div style="width: 100%%; font-size: 1.125rem; font-weight: 400; color: #373737; margin-top: 50px; margin-bottom: 100px;">
                                        해당 이메일로 만들어진 계정이 이미 존재한다! <br />
                                        비밀번호가 기억나지 않는다면, 로그인 페이지의 <b>비밀번호를 잊으셨나요?</b> 기능을 이용하시게.
                                    </div>
                                </div>
                            """);
        }
        else
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
                    "[서부의 바람은, 손끝에서 분다] 이메일 인증번호",
                    String.format(
                            """
                                <div style="display: flex; flex-direction: column; align-items: center; margin: 20px;">
                                    <div style="width: 100%%; font-size: 1.125rem; font-weight: 400; color: #373737; margin-top: 50px;">
                                        다음 인증번호를 <b>인증번호 입력란</b>에 입력하시게. <br />
                                        인증번호가 틀리면 인증번호를 다시 전송해야 하니 주의하도록!
                                    </div>
                                    <div style="font-size: 2.5rem; font-weight: 600; color: #373737; margin-top: 100px; margin-bottom: 100px;">%s</div>
                                </div>
                            """,
                            verificationCode));
        }
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

    // 비밀번호 초기화
    @Transactional
    public void resetPassword(ResetPasswordReqDto reqDto)
    {
        // 사용자 조회
        Optional<User> userOptional = userRepository.findByEmail(reqDto.getEmail());

        // 계정 존재에 따른 분기 처리
        if(userOptional.isPresent())
        {
            // 새로운 비밀번호 생성
            SecureRandom random = new SecureRandom();
            List<Character> newPasswordList = new ArrayList<>();

            // 영문, 숫자, 특수문자를 최소 1개씩 포함
            newPasswordList.add(PASSWORD_POSSIBLE_ENGLISH.charAt(random.nextInt(PASSWORD_POSSIBLE_ENGLISH.length())));
            newPasswordList.add(PASSWORD_POSSIBLE_NUMBER.charAt(random.nextInt(PASSWORD_POSSIBLE_NUMBER.length())));
            newPasswordList.add(PASSWORD_POSSIBLE_SPECIAL.charAt(random.nextInt(PASSWORD_POSSIBLE_SPECIAL.length())));

            // 나머지 길이만큼 아무 문자나 추가
            for(int i = 3; i < 12; i++) {
                newPasswordList.add(PASSWORD_ALL_POSSIBLE_LETTERS.charAt(random.nextInt(PASSWORD_ALL_POSSIBLE_LETTERS.length())));
            }

            // 순서 섞기
            Collections.shuffle(newPasswordList);

            // 문자열로 변환
            StringBuilder sb = new StringBuilder();
            for(Character c : newPasswordList) {
                sb.append(c);
            }
            String newPassword = sb.toString();

            // 사용자 비밀번호 초기화
            User user = userOptional.get();
            user.resetPassword(bCryptPasswordEncoder.encode(newPassword));

            // 이메일 전송
            emailUtil.sendEmail(
                    reqDto.getEmail(),
                    "[서부의 바람은, 손끝에서 분다] 비밀번호 초기화",
                    String.format(
                            """
                                <div style="display: flex; flex-direction: column; align-items: center; margin: 20px;">
                                    <div style="width: 100%%; font-size: 1.125rem; font-weight: 400; color: #373737; margin-top: 50px;">
                                        여기 자네의 <b>비밀번호</b>가 정상적으로 초기화되었다. <br />
                                        다른 사람이 보지 않도록 주의하도록!
                                    </div>
                                    <div style="font-size: 2.5rem; font-weight: 600; color: #373737; margin-top: 100px; margin-bottom: 100px;">%s</div>
                                </div>
                            """,
                            newPassword));
        }
        else
        {
            // 이메일 전송
            emailUtil.sendEmail(
                    reqDto.getEmail(),
                    "[서부의 바람은, 손끝에서 분다] 비밀번호 초기화",
                    """
                                <div style="display: flex; flex-direction: column; align-items: center; margin: 20px;">
                                    <div style="width: 100%%; font-size: 1.125rem; font-weight: 400; color: #373737; margin-top: 50px; margin-bottom: 100px;">
                                        해당 이메일로 만들어진 계정이 존재하지 않는군... <br />
                                        <b>회원가입</b>을 새로 해야겠구만!
                                    </div>
                                </div>
                            """);
        }
    }

    // access token 재발급
    @Transactional
    public void refreshAccessToken(String refreshToken, HttpServletResponse response)
    {
        // refresh token 유효성 체크
        if(!tokenService.checkToken(refreshToken)) {
            throw new CustomException(CustomExceptionCode.INVALID_REFRESH_TOKEN, null);
        }

        // refresh token에서 사용자 정보 추출
        Long userId = tokenService.getClaims(refreshToken, "userId", Long.class);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, null));

        // refresh token 만료 여부 체크
        if(!user.getRefreshToken().equals(refreshToken)) {
            throw new CustomException(CustomExceptionCode.EXPIRED_REFRESH_TOKEN, null);
        }

        // access token 발급
        String accessToken = tokenService.createToken(TokenType.ACCESS_TOKEN, Map.of("userId", user.getId()));

        // 토큰을 쿠키로 변환
        Cookie accessTokenCookie = tokenService.parseTokenToCookie(accessToken, TokenType.ACCESS_TOKEN);

        // 쿠키를 응답에 포함
        response.addCookie(accessTokenCookie);
    }

    // 기본 프로필 정보 조회
    @Transactional(readOnly = true)
    public GetBasicUserInfoResDto getBasicUserInfo(User user)
    {
        return GetBasicUserInfoResDto.builder()
                .nickname(user.getNickname())
                .level(user.getLevel())
                .exp(user.getExp())
                .requiredExp(levelUtil.getRequiredExp(user.getLevel()))
                .build();
    }
}
