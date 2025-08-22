package com.example.demo.exception.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TableNotAvailableException extends RuntimeException {

    public TableNotAvailableException() {
        super("İstenen masa şu anda müsait değil.");
    }

    public TableNotAvailableException(String message) {
        super(message);
    }

    public TableNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

