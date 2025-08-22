package com.example.demo.validation;

import com.example.demo.dto.request.ProductIngredientRequestDTO;
import com.example.demo.exception.productingredient.InvalidProductIngredientException;
import com.example.demo.repository.ProductIngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductIngredientValidator {

    private final ProductIngredientRepository productIngredientRepository;

    @Autowired
    public ProductIngredientValidator(ProductIngredientRepository productIngredientRepository) {
        this.productIngredientRepository = productIngredientRepository;
    }

    public void validateCreate(ProductIngredientRequestDTO requestDTO) {
        // Check if the combination already exists
        if (productIngredientRepository.findByProductIdAndStockId(
                requestDTO.getProductId(), requestDTO.getIngredientId()).isPresent()) {
            throw new InvalidProductIngredientException("ProductIngredient already exists for this product and ingredient combination");
        }
    }

    public void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductIngredientException("Quantity must be greater than zero");
        }
    }
}