package ru.chernevich.service;

import ru.chernevich.entity.AppDocument;
import ru.chernevich.entity.AppPhoto;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
}
