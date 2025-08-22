package com.example.demo.enums;

/**
 * Rezervasyon durumları için sabit ID değerleri
 */
public final class ReservationStatusConstants {
    
    public static final int CONFIRMED = 1;      // Onaylandı
    public static final int CANCELLED = 2;      // İptal edildi
    public static final int COMPLETED = 3;      // Tamamlandı
    public static final int NO_SHOW = 4;        // Gelmedi
    public static final int PENDING = 5;        // Beklemede
    
    private ReservationStatusConstants() {
        // Utility class, prevent instantiation
    }
    
    /**
     * Status ID'nin geçerli olup olmadığını kontrol eder
     */
    public static boolean isValidStatusId(Integer statusId) {
        return statusId != null && statusId >= 1 && statusId <= 5;
    }
    
    /**
     * Status ID'ye karşılık gelen Türkçe ismi döndürür
     */
    public static String getStatusNameInTurkish(Integer statusId) {
        if (statusId == null) return "Bilinmiyor";
        
        return switch (statusId) {
            case CONFIRMED -> "Onaylandı";
            case CANCELLED -> "İptal Edildi";
            case COMPLETED -> "Tamamlandı";
            case NO_SHOW -> "Gelmedi";
            case PENDING -> "Beklemede";
            default -> "Bilinmiyor";
        };
    }
    
    /**
     * Status ID'ye karşılık gelen İngilizce ismi döndürür
     */
    public static String getStatusName(Integer statusId) {
        if (statusId == null) return "unknown";
        
        return switch (statusId) {
            case CONFIRMED -> "confirmed";
            case CANCELLED -> "cancelled";
            case COMPLETED -> "completed";
            case NO_SHOW -> "no_show";
            case PENDING -> "pending";
            default -> "unknown";
        };
    }
}
