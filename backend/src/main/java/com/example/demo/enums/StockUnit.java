package com.example.demo.enums;

public enum StockUnit {
    L("Litre"),
    KG("Kilogram"),
    ADET("Adet");

    private final String displayName;

    StockUnit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
