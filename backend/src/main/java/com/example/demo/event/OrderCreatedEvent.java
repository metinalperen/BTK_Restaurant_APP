package com.example.demo.event;

import com.example.demo.model.Order;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event fired when a new order is created.
 * Used for real-time analytics updates to maintain O(1) performance.
 */
public class OrderCreatedEvent extends ApplicationEvent {
    
    private final Order order;
    
    public OrderCreatedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    
    public Order getOrder() {
        return order;
    }
}
