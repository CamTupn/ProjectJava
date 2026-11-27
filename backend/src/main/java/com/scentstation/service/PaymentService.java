package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.contract.PaymentNotice;
import com.scentstation.contract.PaymentPort;
import com.scentstation.contract.PaymentView;
import com.scentstation.entity.Order;
import com.scentstation.entity.Payment;
import com.scentstation.entity.PaymentStatus;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.PaymentMapper;
import com.scentstation.repository.OrderRepository;
import com.scentstation.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentService implements PaymentPort {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          PaymentMapper paymentMapper,
                          CurrentUserService currentUserService,
                          ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentMapper = paymentMapper;
        this.currentUserService = currentUserService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PaymentView createPayment(Long orderId) {
        Order order = findOrder(orderId);
        validatePaymentCreation(order);

        Payment existing = paymentRepository
                .findFirstByOrder_IdAndStatusOrderByCreatedAtDesc(orderId, PaymentStatus.PENDING)
                .orElse(null);
        if (existing != null) {
            return paymentMapper.toView(existing);
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentRef("MOCK-" + UUID.randomUUID());
        payment.setProvider("MOCK");
        payment.setAmount(order.getUnitPrice());
        payment.setCurrency(order.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        try {
            return paymentMapper.toView(paymentRepository.saveAndFlush(payment));
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException("A payment already exists for this order", ex);
        }
    }

    @Override
    public PaymentView processCallback(PaymentNotice notice) {
        validateNotice(notice);
        Payment payment = paymentRepository.findByPaymentRef(notice.paymentRef())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getOrder().getId().equals(notice.orderId())) {
            throw new ResourceConflictException("Payment does not belong to the supplied order");
        }
        if (payment.getAmount().compareTo(notice.amount()) != 0) {
            throw new ResourceConflictException("Payment amount does not match the order amount");
        }
        if (!payment.getCurrency().equalsIgnoreCase(notice.currency())) {
            throw new ResourceConflictException("Payment currency does not match the order currency");
        }
        rejectGatewayTransactionReuse(payment, notice.gatewayTransactionId());

        if (isTerminal(payment.getStatus())) {
            return paymentMapper.toView(payment);
        }
        if (isOrderExpired(payment.getOrder())) {
            throw new ResourceConflictException("Payment callback arrived after the order expired");
        }

        payment.setStatus(notice.status());
        payment.setGatewayTransactionId(notice.gatewayTransactionId());
        payment.setPaidAt(notice.status() == PaymentStatus.SUCCESS
                ? (notice.paidAt() == null ? LocalDateTime.now() : notice.paidAt())
                : null);
        Payment saved = paymentRepository.saveAndFlush(payment);
        PaymentNotice published = new PaymentNotice(saved.getPaymentRef(), saved.getOrder().getId(),
                saved.getAmount(), saved.getCurrency(), saved.getStatus(),
                saved.getGatewayTransactionId(), saved.getPaidAt(), notice.signature(), false);
        eventPublisher.publishEvent(published);
        return paymentMapper.toView(saved);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Payment> findPayments(Long orderId, PaymentStatus status) {
        List<Payment> payments;
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        if (orderId != null) {
            payments = paymentRepository.findByOrder_IdOrderByCreatedAtDesc(orderId);
        } else {
            payments = current.isPlatformSuperAdmin()
                    ? paymentRepository.findAllByOrderByCreatedAtDesc()
                    : paymentRepository.findAllByOrder_TenantIdOrderByCreatedAtDesc(current.tenantId());
        }
        if (!current.isPlatformSuperAdmin()) {
            payments = payments.stream()
                    .filter(payment -> payment.getOrder() != null
                            && current.tenantId().equals(payment.getOrder().getTenantId()))
                    .toList();
        }
        if (status == null) return payments;
        return payments.stream().filter(payment -> payment.getStatus() == status).toList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Payment> findPaymentsForKiosk(Long orderId, PaymentStatus status) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }
        List<Payment> payments = paymentRepository.findByOrder_IdOrderByCreatedAtDesc(orderId);
        if (status == null) return payments;
        return payments.stream().filter(payment -> payment.getStatus() == status).toList();
    }

    public PaymentView processManagementCallback(PaymentNotice notice) {
        Payment payment = paymentRepository.findByPaymentRef(notice.paymentRef())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        assertTenantAccess(payment);
        return processCallback(notice);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Payment getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        assertTenantAccess(payment);
        return payment;
    }

    private Order findOrder(Long orderId) {
        if (orderId == null) throw new IllegalArgumentException("Order ID is required");
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    }

    private void validatePaymentCreation(Order order) {
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new ResourceConflictException("Payment can only be created for an unpaid order");
        }
        if (isOrderExpired(order)) {
            throw new ResourceConflictException("Order has expired");
        }
        if (order.getUnitPrice() == null || order.getCurrency() == null || order.getCurrency().isBlank()) {
            throw new ResourceConflictException("Order does not contain a valid payment amount");
        }
    }

    private boolean isOrderExpired(Order order) {
        return "EXPIRED".equals(order.getStatus())
                || (order.getExpiresAt() != null && order.getExpiresAt().isBefore(LocalDateTime.now()));
    }

    private void validateNotice(PaymentNotice notice) {
        if (notice == null || notice.paymentRef() == null || notice.paymentRef().isBlank()
                || notice.orderId() == null || notice.amount() == null || notice.currency() == null
                || notice.status() == null) {
            throw new IllegalArgumentException("Invalid payment callback");
        }
        if (notice.status() == PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Payment callback cannot set PENDING status");
        }
    }

    private void rejectGatewayTransactionReuse(Payment payment, String gatewayTransactionId) {
        if (gatewayTransactionId == null || gatewayTransactionId.isBlank()) return;
        paymentRepository.findByGatewayTransactionId(gatewayTransactionId)
                .filter(existing -> !existing.getId().equals(payment.getId()))
                .ifPresent(existing -> {
                    throw new ResourceConflictException("Gateway transaction ID has already been processed");
                });
    }

    private boolean isTerminal(PaymentStatus status) {
        return status == PaymentStatus.SUCCESS || status == PaymentStatus.FAILED
                || status == PaymentStatus.CANCELLED || status == PaymentStatus.EXPIRED
                || status == PaymentStatus.REFUNDED;
    }

    private void assertTenantAccess(Payment payment) {
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        if (!current.isPlatformSuperAdmin()
                && !payment.getOrder().getTenantId().equals(current.tenantId())) {
            throw new ResourceNotFoundException("Payment not found");
        }
    }
}
