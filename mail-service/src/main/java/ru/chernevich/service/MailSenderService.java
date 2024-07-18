package ru.chernevich.service;

import ru.chernevich.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
