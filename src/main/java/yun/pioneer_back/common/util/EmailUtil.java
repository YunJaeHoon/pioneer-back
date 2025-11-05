package yun.pioneer_back.common.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import yun.pioneer_back.common.exception.CustomException;
import yun.pioneer_back.common.exception.CustomExceptionCode;

@Service
@RequiredArgsConstructor
public class EmailUtil
{
    private final JavaMailSender mailSender;

    // 이메일 전송
    public void sendEmail(String to, String subject, String content)
    {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            // 수신 이메일, 제목, 내용 설정
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(content, true);

            // 이메일 전송
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new CustomException(CustomExceptionCode.SEND_EMAIL_ERROR, e.getMessage());
        }
    }
}
