package ru.chernevich.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.chernevich.controller.UpdateProcessor;
import ru.chernevich.service.AnswerConsumer;

@RequiredArgsConstructor
@Service
public class AnswerConsumerImpl implements AnswerConsumer {

    private final UpdateProcessor updateProcessor;

    @Override
    @RabbitListener(queues = "${spring.rabbitmq.queues.answer-message}")
    public void consume(SendMessage sendMessage) {
        updateProcessor.setView(sendMessage);
    }
}
