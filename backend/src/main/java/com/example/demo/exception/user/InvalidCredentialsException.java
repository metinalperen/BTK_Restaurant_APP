package com.example.demo.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends UserException {
    public InvalidCredentialsException() { super("Geçersiz email veya şifre."); }
    public InvalidCredentialsException(String message) { super(message); }
}

