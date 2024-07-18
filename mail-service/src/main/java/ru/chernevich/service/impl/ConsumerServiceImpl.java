package ru.chernevich.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.chernevich.dto.MailParams;
import ru.chernevich.service.ConsumerService;
import ru.chernevich.service.MailSenderService;

@RequiredArgsConstructor
@Service
public class ConsumerServiceImpl implements ConsumerService {

    private final MailSenderService mailSenderService;

    @Override
    @RabbitListener(queues = "${spring.rabbitmq.queues.registration-mail}")
    public void consumeRegistrationMail(MailParams mailParams) {
        mailSenderService.send(mailParams);
    }
}
