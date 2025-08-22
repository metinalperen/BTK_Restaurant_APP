package com.example.demo.dto.request;

import com.example.demo.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;


import java.math.BigDecimal;

public class PaymentRequestDTO {

    @Schema(description = "Hangi siparişin ödemesi?", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;     // Hangi siparişin ödemesi?
    @Schema(description = "Hangi kasiyer aldı?", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cashierId;   // Hangi kasiyer aldı?
    @Schema(description = "Ödeme tutarı", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount; // Tutar
    @Schema(description = "Ödeme yöntemi", example = "CASH", requiredMode = Schema.RequiredMode.REQUIRED)
    private Payment.PaymentMethod method;    // CASH veya POS

    // --- GETTER & SETTER ---
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getCashierId() { return cashierId; }
    public void setCashierId(Long cashierId) { this.cashierId = cashierId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Payment.PaymentMethod getMethod() { return method; }
    public void setMethod(Payment.PaymentMethod method) { this.method = method; }
}
