package com.example.demo.dto.response;

public class ProductAvailableQuantityDTO {
    private Long productId;
    private Long amount;

    public ProductAvailableQuantityDTO() {}

    public ProductAvailableQuantityDTO(Long productId, Long amount) {
        this.productId = productId;
        this.amount = amount;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
