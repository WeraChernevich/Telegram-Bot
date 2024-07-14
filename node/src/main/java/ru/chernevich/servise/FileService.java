package ru.chernevich.servise;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.chernevich.entity.AppDocument;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
}
