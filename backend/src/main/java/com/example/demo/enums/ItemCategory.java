package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemCategory {
    DRINKS("drinks"),
    DESSERTS("desserts"),
    MAIN_DISHES("main_dishes"),
    APPETIZER("appetizer"),
    //Bunları frontende uysun diye koyuyyorum
    ANA_YEMEK("ana_yemek"),
    APARITIFLER("aparitifler"),
    FIRIN("firin"),
    IZGARALAR("izgaralar"),
    KAHVALTILIKLAR("kahvaltiliklar"),
    ICECEKLER("icecekler"),
    TATLILAR("tatlilar");


    private final String value;

    ItemCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    // Static method to convert from database string to enum
    public static ItemCategory fromString(String value) {
        for (ItemCategory category : ItemCategory.values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
