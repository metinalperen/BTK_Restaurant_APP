package com.example.demo.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.OrderRequestDTO;
import com.example.demo.dto.response.OrderResponseDTO;
import com.example.demo.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(
        name = "Order Management",
        description = "APIs for managing orders (CRUD operations, filtering, and status updates)."
)
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @Autowired // dependency injection
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Sipariş oluşturma endpoint'i
    @PostMapping
    @Operation(
            summary = "Create a new order",
            description = "Creates a new order with the provided details."
    )
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        OrderResponseDTO response = orderService.createOrder(orderRequestDTO);
        URI location = URI.create("/api/orders/" + response.getOrderId());
        return ResponseEntity.created(location).body(response);
    }

    //Belirli Id'e sahip siparişi getir.
    @GetMapping("/{id}")
    @Operation(
            summary = "Get order by ID",
            description = "Retrieves a specific order by its ID."
    )
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "ID of the order to retrieve", required = true)
            @PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    //Sipariş güncelle
    @PutMapping("/{id}")
    @Operation(
            summary = "Update an order",
            description = "Updates an existing order's details."
    )
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @Parameter(description = "ID of the order to update", required = true)
            @PathVariable Long id,
            @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        OrderResponseDTO updatedOrder = orderService.updateOrder(id, orderRequestDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    //Sipariş sil
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an order",
            description = "Deletes an existing order by its ID."
    )
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID of the order to delete", required = true)
            @PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    //Tüm siparişleri listele.
    @GetMapping
    @Operation(
            summary = "List orders",
            description = "Lists orders; optionally filter by isCompleted and/or tableId."
    )
    public ResponseEntity<List<OrderResponseDTO>> listOrders(
            @RequestParam(required = false) Boolean isCompleted,
            @RequestParam(required = false) Long tableId
    ) {
        List<OrderResponseDTO> orders = orderService.getOrdersFiltered(isCompleted, tableId);
        return ResponseEntity.ok(orders);
    }

    /*Mantık Endpointleri
    Bu endpointler, verilen siparişlerin gerçek dünya mantığına uygun olarak işlenmesini sağlar.
    Örn: Sipariş verildiğinde stok kontrolü ve işlenmesi, sipariş durumu güncelleme, sipariş iptali gibi işlemler.
    */

    // Sipariş oluşturma endpoint'i
    @Deprecated
    @PostMapping("/make-order")
    @Operation(
            summary = "Make a new order",
            description = "Creates a new order and processes it according to business logic."
    )
    public ResponseEntity<OrderResponseDTO> makeOrder(
            @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        OrderResponseDTO response = orderService.createOrder(orderRequestDTO);
        try {
            orderService.processOrder(response.getOrderId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 1) Giriş yapmış kullanıcının (garsonun) kendi siparişleri
    @GetMapping("/my")
    @Operation(
            summary = "Get my orders",
            description = "JWT ile kimliği doğrulanmış kullanıcının (garson) kendi aldığı siparişleri listeler."
    )
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(Authentication authentication) {
        List<OrderResponseDTO> orders = orderService.getMyOrders(authentication.getName());
        return ResponseEntity.ok(orders);
    }

    // 2) Belirli bir kullanıcıya (garsona) ait siparişler - yönetici/şef ekranları için
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get orders by userId",
            description = "Belirtilen kullanıcıya (garsona) ait tüm siparişleri listeler."
    )
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(
            @Parameter(description = "Kullanıcı (garson) ID", required = true)
            @PathVariable Long userId) {
        List<OrderResponseDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // Aktif sipariş bulunan masa ID'lerini döner
    @GetMapping("/active-table-ids")
    @Operation(
            summary = "Get active table IDs",
            description = "Aktif sipariş bulunan (is_completed=false) masa ID'lerini listeler."
    )
    public ResponseEntity<List<Long>> getActiveTableIds() {
        List<Long> activeTableIds = orderService.getActiveTableIds();
        return ResponseEntity.ok(activeTableIds);
    }

    /****** Yeni sipariş endpointleri (eskiler şimdilik silinmedi) *****/
    // 1) Masa için açık (isCompleted=false) siparişi getir
    @GetMapping("/table/{tableId}/open")
    @Operation(summary = "Get open (unpaid) order of a table")
    public ResponseEntity<OrderResponseDTO> getOpenOrderOfTable(@PathVariable Long tableId) {
        return orderService.getOpenOrderByTable(tableId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build()); // açık sipariş yoksa 204
    }

    // 2) Tam senkron upsert (yoksa oluşturur, varsa günceller)
    // Not: Mevcut OrderRequestDTO kullanıyoruz. userId'yi request'ten DEĞİL, Authentication'dan alacağız.
    @PostMapping("/upsert-sync")
    @Operation(summary = "Create/Update (sync) open order for a table")
    public ResponseEntity<OrderResponseDTO> upsertOrderSync(
            @Valid @RequestBody OrderRequestDTO orderRequestDTO,
            Authentication authentication) {
        OrderResponseDTO response = orderService.upsertOrderSync(orderRequestDTO, authentication);
        return ResponseEntity.ok(response);
    }

    // Proper Endpoint for actually finalizing an order for the usage of products to trickle down to stocks and stock movements.
    // POST /orders/:id/finalize

    @PostMapping("/{id}/finalize")
    @Operation(
            summary = "Finalize an order",
            description = "Finalizes an order, updating its status and adjusting stock levels accordingly."
    )
    public ResponseEntity<OrderResponseDTO> finalizeOrder(
            @Parameter(description = "ID of the order to finalize", required = true)
            @PathVariable Long id) {
        orderService.processOrder(id);
        OrderResponseDTO finalizedOrder = orderService.getOrderById(id);
        return ResponseEntity.ok(finalizedOrder);
    }
}
