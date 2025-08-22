package com.example.demo.validation;

import com.example.demo.dto.request.StockRequestDTO;
import com.example.demo.enums.StockUnit;
import com.example.demo.exception.stock.StockValidationException;
import com.example.demo.utils.BDH;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StockValidator {

    public void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new StockValidationException("Malzeme adı boş olamaz.");
        }
        // İsteğe bağlı: normalize edilmiş boşluk kontrolü
        if (name.trim().replaceAll("\\s+", " ").length() == 0) {
            throw new StockValidationException("Malzeme adı geçersiz.");
        }
    }

    // Değişiklik: String yerine StockUnit alıyor
    public void validateUnit(StockUnit unit) {
        if (unit == null) {
            throw new StockValidationException("Ölçü birimi (unit) boş olamaz.");
        }
        // Enum kullanıldığı için ekstra 'geçerli değer' kontrolüne gerek yok.
        // (LITRE, KILOGRAM, ADET gibi)
    }

    public void validateStockQuantity(BigDecimal stockQuantity) {
        if (stockQuantity == null || BDH.isNegative(stockQuantity)) {
            throw new StockValidationException("Stok miktarı 0 veya daha büyük olmalıdır.");
        }
    }

    public void validateStockRequest(StockRequestDTO dto) {
        validateName(dto.getName());
        validateUnit(dto.getUnit());            // Artık StockUnit bekliyor
        validateStockQuantity(dto.getStockQuantity());
    }
}
