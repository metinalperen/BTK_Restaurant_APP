package com.example.demo.exception.product;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class ProductReferencedInOrder extends ProductException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Long productId;
    private final String productName;

    public ProductReferencedInOrder(Long productId, String productName) {
        super(String.format("Product '%s' (ID: %d) cannot be deleted because it is referenced in existing orders", productName, productId));
        this.productId = productId;
        this.productName = productName;
    }

    public ProductReferencedInOrder(String message, Long productId, String productName) {
        super(message);
        this.productId = productId;
        this.productName = productName;
    }
}
