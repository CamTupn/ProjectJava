package com.scentstation.repository;

import com.scentstation.entity.Payment;
import com.scentstation.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentRef(String paymentRef);
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);
    Optional<Payment> findFirstByOrder_IdAndStatusOrderByCreatedAtDesc(Long orderId, PaymentStatus status);
    List<Payment> findByOrder_IdOrderByCreatedAtDesc(Long orderId);
    List<Payment> findAllByOrder_TenantIdOrderByCreatedAtDesc(Long tenantId);
    List<Payment> findAllByOrderByCreatedAtDesc();
}
