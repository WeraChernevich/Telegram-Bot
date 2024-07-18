package ru.chernevich.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import ru.chernevich.dao.AppDocumentDAO;
import ru.chernevich.dao.AppPhotoDAO;
import ru.chernevich.entity.AppDocument;
import ru.chernevich.entity.AppPhoto;
import ru.chernevich.service.FileService;
import ru.chernevich.utils.Decoder;

@Log4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final AppDocumentDAO appDocumentDAO;

    private final AppPhotoDAO appPhotoDAO;

    private final Decoder decoder;

    @Override
    public AppDocument getDocument(String hash) {
        var id = decoder.idOf(hash);
        if (id == null) {
            return null;
        }
        return appDocumentDAO.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String hash) {
        var id = decoder.idOf(hash);
        if (id == null) {
            return null;
        }
        return appPhotoDAO.findById(id).orElse(null);
    }
}
