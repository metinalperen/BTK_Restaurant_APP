package com.example.demo.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ItemCategoryConverter implements AttributeConverter<ItemCategory, String> {

    @Override
    public String convertToDatabaseColumn(ItemCategory category) {
        if (category == null) {
            return null;
        }
        return category.getValue();
    }

    @Override
    public ItemCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        try {
            return ItemCategory.fromString(dbData);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown database value for ItemCategory: " + dbData, e);
        }
    }
}
