package com.project.sidefit.domain.service.mail;

import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    // TODO message 내용 고정되지 않게 메시지를 파라미터로 받아서 해당 메시지를 전달하는 형태로
    @Async
    public void sendMail(SimpleMailMessage mailMessage) {

        javaMailSender.send(mailMessage);
    }
}
