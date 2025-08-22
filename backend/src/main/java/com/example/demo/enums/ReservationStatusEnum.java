package com.example.demo.enums;

/**
 * Rezervasyon durumları için enum
 */
public enum ReservationStatusEnum {
    confirmed,    // Onaylandı
    cancelled,    // İptal edildi
    no_show,      // Gelmedi
    completed,    // Tamamlandı
    pending       // Beklemede
}
