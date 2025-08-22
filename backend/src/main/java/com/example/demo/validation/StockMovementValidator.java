package com.example.demo.validation;

import com.example.demo.dto.request.StockMovementRequestDTO;
import com.example.demo.enums.StockMovementEnum;
import com.example.demo.exception.stockmovement.StockMovementValidationException;
import com.example.demo.utils.BDH;
import org.springframework.stereotype.Component;

@Component
public class StockMovementValidator {

    public void validateStockMovement(StockMovementRequestDTO dto) {
        if (dto.getStockId() == null) {
            throw new StockMovementValidationException("Stock ID cannot be null.");
        }

        if (dto.getChange() == null || BDH.isZero(dto.getChange()) ) {
            throw new StockMovementValidationException("Change must not be null or zero.");
        }

        StockMovementEnum reason = dto.getReason();
        if (reason == null) {
            throw new StockMovementValidationException("Reason cannot be null.");
        }
    }

    public void validateReason(String reason) {
        try {
            StockMovementEnum.valueOf(reason.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new StockMovementValidationException("Invalid reason. Valid reasons are: " +
                    java.util.Arrays.toString(StockMovementEnum.values()));
        }
    }
}