package com.example.demo.controller;

import com.example.demo.model.OrderItem;
import com.example.demo.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Order Item Management",
        description = "APIs for managing order items (CRUD operations)."
)
@RestController
@RequestMapping("/api/order-items")
@CrossOrigin(origins = "*")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Autowired
    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    //Bir sipariş kaleminin miktarını güncelle
    @PutMapping("/{id}")
    @Operation(
            summary = "Update an order item",
            description = "Updates an existing order item."
    )
    public ResponseEntity<OrderItem> updateOrderItem(
            @Parameter(description = "ID of the order item to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "New quantity for the order item", required = true)
            @RequestParam int quantity) {

        OrderItem updatedItem = orderItemService.updateOrderItem(id, quantity);
        return ResponseEntity.ok(updatedItem);
    }
    //Bir siparişin içindeki belirli bir ürünü (order item) tamamen silmek.
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an order item",
            description = "Deletes an order item by its ID."
    )
    public ResponseEntity<Void> deleteOrderItem(
            @Parameter(description = "ID of the order item to delete", required = true)
            @PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
        return ResponseEntity.noContent().build();
    }
}
