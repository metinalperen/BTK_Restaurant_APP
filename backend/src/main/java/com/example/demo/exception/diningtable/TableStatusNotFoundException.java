package com.example.demo.exception.diningtable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Belirli bir TableStatus bulunamadığında fırlatılan özel hata sınıfı.
 * @ResponseStatus anotasyonu, bu hata fırlatıldığında HTTP 404 Not Found yanıtı dönmesini sağlar.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TableStatusNotFoundException extends RuntimeException {

    /**
     * Hata mesajını alan constructor.
     * @param message Hata mesajı
     */
    public TableStatusNotFoundException(String message) {
        super(message);
    }

    /**
     * Hata mesajını ve neden olan hatayı (cause) alan constructor.
     * @param message Hata mesajı
     * @param cause Neden olan hata
     */
    public TableStatusNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
