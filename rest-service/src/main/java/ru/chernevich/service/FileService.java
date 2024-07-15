package ru.chernevich.service;

import org.springframework.core.io.FileSystemResource;
import ru.chernevich.entity.AppDocument;
import ru.chernevich.entity.AppPhoto;
import ru.chernevich.entity.BinaryContent;

import java.nio.file.FileSystem;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
}
