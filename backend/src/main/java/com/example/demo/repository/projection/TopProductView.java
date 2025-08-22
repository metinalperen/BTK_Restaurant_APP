package com.example.demo.repository.projection;

import java.math.BigDecimal;

/**
 * JPA projection: DB tarafında aggregate edilen alanların doğrudan okunması.
 */
public interface TopProductView {
    Long getProductId();
    String getProductName();
    Long getTotalQuantity();   // SUM(oi.quantity)
    Long getOrderCount();      // COUNT(DISTINCT o.id)
    BigDecimal getTotalRevenue(); // SUM(oi.totalPrice)
}


