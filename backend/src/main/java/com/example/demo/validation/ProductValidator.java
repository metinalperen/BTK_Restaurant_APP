package com.example.demo.validation;

import com.example.demo.dto.request.ProductRequestDto;

public class ProductValidator {

    public static void validateProduct(ProductRequestDto productDto) {
        if (productDto.getName() == null || productDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ürün adı boş olamaz.");
        }

        if (productDto.getPrice() == null) {
            throw new IllegalArgumentException("Ürün fiyatı boş olamaz.");
        }

        if (productDto.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Ürün fiyatı negatif olamaz.");
        }

        if (productDto.getCategory() == null || productDto.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Kategori boş olamaz.");
        }
    }
}


