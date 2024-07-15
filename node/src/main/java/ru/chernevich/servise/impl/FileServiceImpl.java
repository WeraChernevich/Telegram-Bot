package ru.chernevich.servise.impl;

import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.chernevich.dao.AppDocumentDAO;
import ru.chernevich.dao.AppPhotoDAO;
import ru.chernevich.dao.BinaryContentDAO;
import ru.chernevich.entity.AppDocument;
import ru.chernevich.entity.AppPhoto;
import ru.chernevich.entity.BinaryContent;
import ru.chernevich.exeptions.UploadFileException;
import ru.chernevich.servise.FileService;
import ru.chernevich.servise.enums.LinkType;
import ru.chernevich.utils.CryptoTool;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Log4j
@Service
public class FileServiceImpl implements FileService {
    @Value("${token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    @Value("${link.address}")
    private String linkAddress; //ссылка на файл
    private final AppDocumentDAO appDocumentDAO;
    private final AppPhotoDAO appPhotoDAO;
    private final BinaryContentDAO binaryContentDAO;
    private final CryptoTool cryptoTool;

    public FileServiceImpl(AppDocumentDAO appDocumentDAO, AppPhotoDAO appPhotoDAO, BinaryContentDAO binaryContentDAO, CryptoTool cryptoTool) {
        this.appDocumentDAO = appDocumentDAO;
        this.appPhotoDAO = appPhotoDAO;
        this.binaryContentDAO = binaryContentDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        Document telegramDoc = telegramMessage.getDocument();
        String fileId = telegramDoc.getFileId();
        ResponseEntity<String> responseEntity = getFilePath(fileId);
        if(responseEntity.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(responseEntity);
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDAO.save(transientAppDoc);
        }
        else {
            throw new UploadFileException(responseEntity);
        }
    }

    @Override
    public AppPhoto processPhoto(Message telegramMessage) {
        var photoSizeCount = telegramMessage.getPhoto().size();
        var photoIndex = photoSizeCount > 1 ? telegramMessage.getPhoto().size() - 1 : 0; //первый файл, если есть картинки
        PhotoSize telegramPhoto = telegramMessage.getPhoto().get(photoIndex);
        String fileId = telegramPhoto.getFileId();
        ResponseEntity<String> responseEntity = getFilePath(fileId);
        if(responseEntity.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(responseEntity);
            AppPhoto transientAppDoc = buildTransientAppPhoto(telegramPhoto, persistentBinaryContent);
            return appPhotoDAO.save(transientAppDoc);
        }
        else {
            throw new UploadFileException(responseEntity);
        }
    }

    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> responseEntity) {
        String filePath = getFilePath(responseEntity);
        byte[] fileInByte = downloadFile(filePath);
        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte)
                .build();
        BinaryContent persistentBinaryContent = binaryContentDAO.save(transientBinaryContent);
        return persistentBinaryContent; //возвращаем постоянную копию
    }

    private String getFilePath(ResponseEntity<String> responseEntity) {
        JSONObject jsonObject = new JSONObject(responseEntity.getBody());
        return String.valueOf(jsonObject
               .getJSONObject("result")
               .getString("file_path"));
    }


    private AppDocument buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();
    }

    private AppPhoto buildTransientAppPhoto(PhotoSize telegramPhoto,
                                            BinaryContent persistentBinaryContent) {
        return AppPhoto.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .telegramFileId(telegramPhoto.getFileId())
                .binaryContent(persistentBinaryContent)
                .fileSize(telegramPhoto.getFileSize())
                .build();
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token)
                .replace("{file_path}", filePath);
        URL urlObject = null;
        try {
            urlObject = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e); //проверка на корректность ссылки
        }
        try (InputStream is = urlObject.openStream()){
            return is.readAllBytes(); //загрузка файла в память
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String generateLink(Long docId, LinkType linkType) {
        var hash = cryptoTool.hashOf(docId);
        return "http://" + linkAddress + "/" + linkType + "?id" + hash;
    }

}
