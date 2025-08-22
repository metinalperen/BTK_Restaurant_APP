package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import com.example.demo.event.OrderCompletedEvent;

import com.example.demo.dto.request.PaymentRequestDTO;
import com.example.demo.dto.response.PaymentResponseDTO;
import com.example.demo.model.Order;
import com.example.demo.model.Payment;
import com.example.demo.model.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          UserRepository userRepository,
                          ActivityLogService activityLogService,
                          ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
        this.eventPublisher = eventPublisher;
    }

    // 💰 Ödeme kaydet (DTO kullanarak)
    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + dto.getOrderId()));
        User cashier = userRepository.findById(dto.getCashierId())
                .orElseThrow(() -> new RuntimeException("Cashier not found with id: " + dto.getCashierId()));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setCashier(cashier);
        payment.setAmount(dto.getAmount());
        payment.setMethod(dto.getMethod());
        payment.setCreatedAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        
        // Ödeme yapıldıktan sonra order'ı tamamlanmış olarak işaretle
        System.out.println("PAYMENT SERVICE: Order " + order.getId() + " is_completed öncesi: " + order.isCompleted());
        order.setCompleted(true);
        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);
        System.out.println("PAYMENT SERVICE: Order " + updatedOrder.getId() + " is_completed sonrası: " + updatedOrder.isCompleted());
        
        // Create activity log
        ObjectNode details = activityLogService.createDetailsNode(
            "Payment created by cashier " + cashier.getName() + " for order " + order.getId(),
            "orderId", order.getId().toString(),
            "cashierId", cashier.getId().toString(),
            "amount", dto.getAmount().toString(),
            "method", dto.getMethod().name()
        );

        activityLogService.logActivity(cashier.getId(), "CREATE", "PAYMENT", saved.getId(), details);
        
        // 🔥 REAL-TIME ANALYTICS: Publish event for order completion
        eventPublisher.publishEvent(new OrderCompletedEvent(this, updatedOrder));
        log.info("OrderCompletedEvent published for completed order ID: {}", updatedOrder.getId());
        
        return mapToDTO(saved);
    }

    // 🔁 Payment -> DTO dönüşümü
    private PaymentResponseDTO mapToDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setMethod(payment.getMethod().name());
        dto.setOrder(payment.getOrder().getId());
        dto.setCashier(payment.getCashier().getId());
        return dto;
    }

    // 🔍 Tüm ödemeleri listele (DTO olarak)
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 🔍 ID’ye göre ödeme bul (DTO olarak)
    public Optional<PaymentResponseDTO> getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::mapToDTO);
    }

    // 🗑️ Ödeme sil
    public void deletePayment(Long id) {
        // Get payment info before deletion for logging
        Payment payment = paymentRepository.findById(id).orElse(null);
        
        paymentRepository.deleteById(id);
        
        // Create activity log
        if (payment != null) {
            ObjectNode details = activityLogService.createDetailsNode(
                "Payment deleted for order " + payment.getOrder().getId(),
                "orderId", payment.getOrder().getId().toString(),
                "amount", payment.getAmount().toString(),
                "method", payment.getMethod().name()
            );
            Long actorUserId = (payment != null && payment.getCashier() != null) ? payment.getCashier().getId() : null;
            activityLogService.logActivity(actorUserId, "DELETE", "PAYMENT", id, details);
        }
    }

    // 🔍 Siparişe ait ödemeleri listele (DTO olarak)
    public List<PaymentResponseDTO> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 🔍 Kasiyerin aldığı ödemeleri listele (DTO olarak)
    public List<PaymentResponseDTO> getPaymentsByCashier(Long cashierId) {
        return paymentRepository.findByCashierId(cashierId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}