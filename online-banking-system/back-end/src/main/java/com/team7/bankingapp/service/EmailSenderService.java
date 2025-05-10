package com.team7.bankingapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    @Value("${mail.sender.name}")
    private String senderName;

    @Value("${mail.sender.address}")
    private String senderAddress;

    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        String subject = "Reset Your Password";
        String link = frontendBaseUrl + "/change-password?token=" + token;
        String content = """
                <p>Hello,</p>
                <p>You requested to reset your password. Click the link below to proceed:</p>
                <p><a href="%s">%s</a></p>
                <p>If you didn't request this, you can safely ignore this email.</p>
                """.formatted(link, link);

        sendHtmlEmail(to, subject, content);
    }

    public void sendEmailChangeEmail(String to, String token) throws MessagingException {
        String subject = "Update Your Email Address";
        String link = frontendBaseUrl + "/change-email?token=" + token;
        String content = """
                <p>Hi there,</p>
                <p>We received a request to update the email address on your account.</p>
                <p>To continue, please click the link below to enter your new email address:</p>
                <p><a href="%s">%s</a></p>
                <p>If you didn't request this, feel free to ignore this message.</p>
                """.formatted(link, link);

        sendHtmlEmail(to, subject, content);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(senderAddress, senderName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Failed to encode sender name", e);
        }
    }
}
