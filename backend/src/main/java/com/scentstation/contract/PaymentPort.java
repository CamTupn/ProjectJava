package com.scentstation.contract;

public interface PaymentPort {
    PaymentView createPayment(Long orderId);
    PaymentView processCallback(PaymentNotice notice);
}
