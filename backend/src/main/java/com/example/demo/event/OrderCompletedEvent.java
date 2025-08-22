package com.example.demo.event;

import com.example.demo.model.Order;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event fired when an order is marked as completed.
 * Used for real-time analytics updates to maintain O(1) performance.
 */
public class OrderCompletedEvent extends ApplicationEvent {
    
    private final Order order;
    
    public OrderCompletedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    
    public Order getOrder() {
        return order;
    }
}

