package com.example.demo.event;

import com.example.demo.model.Order;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event fired when an existing order is updated.
 * Used for real-time analytics updates to maintain O(1) performance.
 */
public class OrderUpdatedEvent extends ApplicationEvent {
    
    private final Order order;
    
    public OrderUpdatedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    
    public Order getOrder() {
        return order;
    }
}

