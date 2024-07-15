package ru.chernevich.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.chernevich.service.FileService;

import java.io.IOException;

@Log4j
@RequestMapping("/file")
@RestController
public class FileController {
    private FileService fileService; //создаем сервис для работы с файлами

    public FileController(FileService fileService) {
        this.fileService = fileService; //создаем сервис для работы с файлами
    }

    @RequestMapping(method = RequestMethod.GET, value = "/get-doc")
    public void getDoc(@RequestParam("id") String id, HttpServletResponse response){
        var doc = fileService.getDocument(id);
        if(doc == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
    }
        response.setContentType(MediaType.parseMediaType(doc.getMimeType()).toString());
        response.setHeader("Content-Disposition", "attachment; filename=" + doc.getDocName()); //добавляем заголовок Content-Disposition для отображения файла
        response.setStatus(HttpServletResponse.SC_OK);

        var binaryContent = doc.getBinaryContent(); //получаем бинарный контент
        try {
            var out = response.getOutputStream(); //получаем поток бинарного контента
            out.write(binaryContent.getFileAsArrayOfBytes());
            out.close();
        } catch (IOException e) {
            log.error(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //если файл не найден, возвращаем ошибку
        }

    }

    @RequestMapping(method = RequestMethod.GET, value = "/get-photo")
    public void getPhoto(@RequestParam("id") String id, HttpServletResponse response){
        var photo = fileService.getPhoto(id);
        if(photo == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return; //если файл не найден, возвращаем ошибку
        }

        response.setContentType(MediaType.IMAGE_JPEG.toString());
        response.setHeader("Content-Disposition", "attachment"); //добавляем заголовок Content-Disposition для отображения файла
        response.setStatus(HttpServletResponse.SC_OK);

        var binaryContent = photo.getBinaryContent(); //получаем бинарный контент
        try {
            var out = response.getOutputStream(); //получаем поток бинарного контента
            out.write(binaryContent.getFileAsArrayOfBytes());
            out.close();
        } catch (IOException e) {
            log.error(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //если файл не найден, возвращаем ошибку
        }

    }
}
