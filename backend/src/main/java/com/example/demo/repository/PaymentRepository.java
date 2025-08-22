package com.example.demo.repository;

import com.example.demo.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 💡 Örnek: Belirli bir siparişin ödemelerini getir
    List<Payment> findByOrderId(Long orderId);

    // 💡 Örnek: Belirli bir kasiyerin aldığı ödemeleri getir
    List<Payment> findByCashierId(Long cashierId);
}
