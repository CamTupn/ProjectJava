package com.scentstation.mapper;

import com.scentstation.contract.PaymentView;
import com.scentstation.dto.response.PaymentResponse;
import com.scentstation.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.from(payment);
    }

    public PaymentView toView(Payment payment) {
        return new PaymentView(payment.getId(), payment.getOrder().getId(),
                payment.getPaymentRef(), payment.getProvider(), payment.getAmount(),
                payment.getCurrency(), payment.getStatus(), payment.getGatewayTransactionId(),
                payment.getPaidAt(), payment.getCreatedAt());
    }
}
