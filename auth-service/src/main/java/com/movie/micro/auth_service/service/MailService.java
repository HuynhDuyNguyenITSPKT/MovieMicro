package com.movie.micro.auth_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final String mailFrom;

    public MailService(JavaMailSender mailSender, @Value("${MAIL_FROM:no-reply@movieticket.local}") String mailFrom) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    public void sendOtp(String email, String otp) {
        if (!StringUtils.hasText(email)) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject("MovieTicket OTP Verification");
            helper.setText("Your OTP is: " + otp + " (expires in 5 minutes)", false);
            mailSender.send(message);
        } catch (MailException | MessagingException ex) {
            LOGGER.warn("Could not send OTP email to {}. Fallback log OTP={}", email, otp);
        }
    }
}
