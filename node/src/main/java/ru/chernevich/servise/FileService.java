package ru.chernevich.servise;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.chernevich.entity.AppDocument;
import ru.chernevich.entity.AppPhoto;
import ru.chernevich.servise.enums.LinkType;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
    AppPhoto processPhoto(Message telegramMessage);
    String generateLink(Long docId, LinkType linkType);

}
