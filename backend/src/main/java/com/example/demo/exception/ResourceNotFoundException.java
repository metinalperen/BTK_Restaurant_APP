package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Genel kaynak bulunamadı hatası için özel exception sınıfı.
 * Bu exception fırlatıldığında HTTP 404 Not Found döner.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Standart mesaj ile exception oluşturur.
     *
     * @param message Hata mesajı
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Mesaj ve neden ile exception oluşturur.
     *
     * @param message Hata mesajı
     * @param cause   İstisnanın sebebi
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Belirli bir entity tipi ve id için anlamlı, standart mesaj oluşturan factory metodu.
     *
     * @param entityName Kaynak tipi adı (örneğin "User", "Order")
     * @param id         Kaynak ID'si
     * @return           ResourceNotFoundException örneği
     */
    public static ResourceNotFoundException forId(String entityName, Object id) {
        return new ResourceNotFoundException(String.format("%s bulunamadı. ID: %s", entityName, id));
    }
}
