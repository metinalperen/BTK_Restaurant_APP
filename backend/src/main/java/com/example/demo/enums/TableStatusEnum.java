package com.example.demo.enums;
   //Masa durumları için enum
public enum TableStatusEnum {
    AVAILABLE("Müsait"),
    OCCUPIED("Dolu"),
    RESERVED("Rezerve"),
    MAINTENANCE("Bakımda");
    private final String displayName;
    TableStatusEnum(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
    //String'den enum'a dönüştürür
    public static TableStatusEnum fromString(String status) {
        for (TableStatusEnum tableStatus : TableStatusEnum.values()) {
            if (tableStatus.name().equalsIgnoreCase(status)) {
                return tableStatus;
            }
        }
        throw new IllegalArgumentException("Geçersiz masa durumu: " + status);
    }
    //Türkçe isimden enum'a dönüştürür
    public static TableStatusEnum fromDisplayName(String displayName) {
        for (TableStatusEnum tableStatus : TableStatusEnum.values()) {
            if (tableStatus.getDisplayName().equalsIgnoreCase(displayName)) {
                return tableStatus;
            }
        }
        throw new IllegalArgumentException("Geçersiz masa durumu: " + displayName);
    }
}