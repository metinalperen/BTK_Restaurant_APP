package com.example.demo.validation;

import com.example.demo.dto.request.StockMovementSummaryRequestDto;
import com.example.demo.repository.ProductRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class StockMovementSummaryValidator {

    private final ProductRepository productRepository;

    public StockMovementSummaryValidator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public String validate(StockMovementSummaryRequestDto dto) {
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            return "Miktar pozitif bir değer olmalıdır.";
        }

        if (dto.getMovementDate() != null && dto.getMovementDate().isAfter(LocalDate.now())) {
            return "Hareket tarihi bugünden sonraki bir tarih olamaz.";
        }

        if (dto.getStockId() == null || !productRepository.existsById(dto.getStockId())) {
            return "Geçerli bir stok ID girilmelidir.";
        }

        return null; // her şey yolundaysa
    }
}

