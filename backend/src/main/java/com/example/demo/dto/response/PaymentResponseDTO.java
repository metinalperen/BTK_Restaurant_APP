package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponseDTO {

    private Long id;
    private Long order;     // sadece order id
    private Long cashier;   // sadece cashier id
    private String method;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    // --- GETTER & SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrder() { return order; }
    public void setOrder(Long order) { this.order = order; }

    public Long getCashier() { return cashier; }
    public void setCashier(Long cashier) { this.cashier = cashier; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}