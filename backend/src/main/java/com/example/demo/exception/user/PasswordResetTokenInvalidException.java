package com.example.demo.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordResetTokenInvalidException extends UserException {
    public PasswordResetTokenInvalidException() { super("Token geçersiz veya süresi dolmuş."); }
    public PasswordResetTokenInvalidException(String message) { super(message); }
}

