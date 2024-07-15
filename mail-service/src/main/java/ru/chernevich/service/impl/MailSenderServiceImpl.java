package ru.chernevich.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.chernevich.dto.MailParams;
import ru.chernevich.service.MailSenderService;

@Service
public class MailSenderServiceImpl implements MailSenderService {
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.activation.uri}")
    private String activationServiceUri;

    public MailSenderServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(MailParams mailParams) {
        var subject = "Активация аккаунта";
        var messageBody = getAvtivationMailBody(mailParams.getId());
        var emailTo = mailParams.getMailTo();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom); //"Администратор"
        message.setTo(emailTo);
        message.setSubject(subject); //"Активация аккаунта"
        message.setText(messageBody);

        javaMailSender.send(message); //"Активация аккаунта"

    }

    private String getAvtivationMailBody(String id) {
        var msg = String.format("Для завершения активации аккаунта нажмите на ссылку:\n%s", activationServiceUri + id);
        return msg.replace("{id}", id);
    }
}
