package com.example.demo.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserPhotoNotFoundException extends UserException {
    public UserPhotoNotFoundException() { super("Kullanıcı fotoğrafı bulunamadı."); }
    public UserPhotoNotFoundException(String message) { super(message); }
}

