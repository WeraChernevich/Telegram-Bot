package ru.chernevich.exeptions;

import org.springframework.http.ResponseEntity;

public class UploadFileException extends RuntimeException{
    public UploadFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadFileException(ResponseEntity<String> message) {;
    }

    public UploadFileException(Throwable cause) {
        super(cause);
    }
}
