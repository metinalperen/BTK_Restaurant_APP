package com.example.demo.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserInactiveException extends UserException {
    public UserInactiveException() { super("Kullanıcı aktif değil."); }
    public UserInactiveException(String message) { super(message); }
}

