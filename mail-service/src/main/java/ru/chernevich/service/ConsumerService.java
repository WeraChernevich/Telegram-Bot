package ru.chernevich.service;

import ru.chernevich.dto.MailParams;

public interface ConsumerService {

    void consumeRegistrationMail(MailParams mailParams);
}
