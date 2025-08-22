package com.example.demo.controller;

import com.example.demo.dto.request.PaymentRequestDTO;
import com.example.demo.dto.response.PaymentResponseDTO;
import com.example.demo.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Payment Management",
        description = "APIs for managing payments (CRUD operations, filtering by order and cashier)."
)
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new payment",
            description = "Creates a new payment for an order."
    )
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO paymentDTO) {
        PaymentResponseDTO savedPayment = paymentService.createPayment(paymentDTO);
        return ResponseEntity.ok(savedPayment);
    }

    @GetMapping
    @Operation(
            summary = "Get all payments",
            description = "Retrieves a list of all payments."
    )
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get payment by ID",
            description = "Retrieves a specific payment by its ID."
    )
    public ResponseEntity<PaymentResponseDTO> getPaymentById(
            @Parameter(description = "ID of the payment to retrieve", required = true)
            @PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a payment",
            description = "Deletes a payment by its ID."
    )
    public ResponseEntity<Void> deletePayment(
            @Parameter(description = "ID of the payment to delete", required = true)
            @PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    @Operation(
            summary = "Get payments by order ID",
            description = "Retrieves all payments for a specific order."
    )
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByOrder(
            @Parameter(description = "ID of the order to retrieve payments for", required = true)
            @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }

    @GetMapping("/cashier/{cashierId}")
    @Operation(
            summary = "Get payments by cashier ID",
            description = "Retrieves all payments processed by a specific cashier."
    )
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByCashier(
            @Parameter(description = "ID of the cashier to retrieve payments for", required = true)
            @PathVariable Long cashierId) {
        return ResponseEntity.ok(paymentService.getPaymentsByCashier(cashierId));
    }
}