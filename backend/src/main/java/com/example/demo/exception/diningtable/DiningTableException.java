//import com.example.demo.exception.diningtable.DiningTableException;
package com.example.demo.exception.diningtable;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;




/**
 * DiningTable (Masa) işlemleri için temel exception sınıfı
 * Diğer spesifik exception sınıfları bu sınıftan türetilir
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DiningTableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Varsayılan mesaj ile exception oluşturur
     */
    public DiningTableException() {
        super("DiningTable işlemi sırasında bir hata oluştu.");
    }

    /**
     * Mesaj ile exception oluşturur
     *
     * @param message Hata mesajı
     */
    public DiningTableException(String message) {
        super(message);
    }

    /**
     * Mesaj ve neden ile exception oluşturur
     *
     * @param message Hata mesajı
     * @param cause   Hatanın nedeni
     */
    public DiningTableException(String message, Throwable cause) {
        super(message, cause);
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Genel validasyon hatası
     */
    public static DiningTableException validationError(String message) {
        return new DiningTableException("Validasyon hatası: " + message);
    }

    /**
     * Veritabanı işlem hatası
     */
    public static DiningTableException databaseError(String operation, Throwable cause) {
        return new DiningTableException("Veritabanı işlemi başarısız: " + operation, cause);
    }

    /**
     * İş kuralı ihlali
     */
    public static DiningTableException businessRuleViolation(String rule, String details) {
        return new DiningTableException("İş kuralı ihlali - " + rule + ": " + details);
    }

    /**
     * Eşzamanlılık hatası
     */
    public static DiningTableException concurrencyError(Long tableId, String operation) {
        return new DiningTableException("Eşzamanlılık hatası (ID: " + tableId + ", İşlem: " + operation +
            "). Masa başka bir kullanıcı tarafından değiştirilmiş olabilir.");
    }

    /**
     * Genel masa işlem hatası
     */
    public static DiningTableException operationFailed(String operation, String details) {
        return new DiningTableException("Masa işlemi başarısız - " + operation + ": " + details);
    }

    /**
     * Konfigürasyon hatası
     */
    public static DiningTableException configurationError(String configType, String message) {
        return new DiningTableException("Konfigürasyon hatası (" + configType + "): " + message);
    }
    /**
     * Masa bulunamadığında fırlatılacak exception
     */
    public static DiningTableException tableNotFound(Long tableId) {
        return new DiningTableException("Masa bulunamadı (ID: " + tableId + ").");
    }

}
