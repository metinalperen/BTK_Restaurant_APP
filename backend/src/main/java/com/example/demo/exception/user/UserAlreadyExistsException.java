package com.example.demo.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("Kullanıcı zaten mevcut.");
    }
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

