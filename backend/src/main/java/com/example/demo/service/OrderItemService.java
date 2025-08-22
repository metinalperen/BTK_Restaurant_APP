package com.example.demo.service;

import com.example.demo.model.OrderItem;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public OrderItem updateOrderItem(Long itemId, int newQuantity) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        item.setQuantity(newQuantity);
        BigDecimal unitPrice = item.getUnitPrice();
        item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(newQuantity)));

        return orderItemRepository.save(item);
    }

    public void deleteOrderItem(Long itemId) {
        if (!orderItemRepository.existsById(itemId)) {
            throw new RuntimeException("Order item not found");
        }
        orderItemRepository.deleteById(itemId);
    }
}
