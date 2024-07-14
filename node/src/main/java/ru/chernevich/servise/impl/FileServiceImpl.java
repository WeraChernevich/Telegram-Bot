package ru.chernevich.servise.impl;

import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.chernevich.dao.AppDocumentDAO;
import ru.chernevich.dao.BinaryContentDAO;
import ru.chernevich.entity.AppDocument;
import ru.chernevich.entity.BinaryContent;
import ru.chernevich.exeptions.UploadFileException;
import ru.chernevich.servise.FileService;

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
    private final AppDocumentDAO appDocumentDAO;
    private final BinaryContentDAO binaryContentDAO;

    public FileServiceImpl(AppDocumentDAO appDocumentDAO, BinaryContentDAO binaryContentDAO) {
        this.appDocumentDAO = appDocumentDAO;
        this.binaryContentDAO = binaryContentDAO;
    }

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        String fileId = telegramMessage.getDocument().getFileId();
        ResponseEntity<String> responseEntity = getFilePath(fileId);
        if(responseEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(responseEntity.getBody());
            String filePath = String.valueOf(jsonObject
                    .getJSONObject("result")
                    .getString("file_path"));
            byte[] fileInByte = downloadFile(filePath);
            BinaryContent transientBinaryContent = BinaryContent.builder()
                    .fileAsArrayOfBytes(fileInByte)
                    .build();

            BinaryContent persistentBinaryContent = binaryContentDAO.save(transientBinaryContent);
            Document telegramDoc = telegramMessage.getDocument();
            AppDocument transiemtAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDAO.save(transiemtAppDoc);
        }
        else {
            throw new UploadFileException(responseEntity.getBody());
        }
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

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers); //заголовки для запроса

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                requestEntity,
                String.class,
                token, fileId
        );
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

}
