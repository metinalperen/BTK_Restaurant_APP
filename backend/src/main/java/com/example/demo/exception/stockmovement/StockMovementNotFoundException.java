package com.example.demo.exception.stockmovement;

/**
 * 404 - Stock movement not found.
 */
public class StockMovementNotFoundException extends StockMovementException {

    public StockMovementNotFoundException(Long id) {
        super(String.format("Stock movement not found with id: %d", id));
    }

    public StockMovementNotFoundException(String message) {
        super(message);
    }

    public static StockMovementNotFoundException forId(Long id) {
        return new StockMovementNotFoundException(id);
    }
}
