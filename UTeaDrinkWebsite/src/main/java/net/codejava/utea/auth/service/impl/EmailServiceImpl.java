package net.codejava.utea.auth.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.auth.service.EmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender sender;
    @Value("${app.mail.from}") String from;

    public EmailServiceImpl(JavaMailSender sender){ this.sender = sender; }

    @Override
    public void send(String to, String subject, String html) {
        try {
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, "UTF-8");
            h.setFrom(from);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            sender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Send mail failed", e);
        }
    }
}