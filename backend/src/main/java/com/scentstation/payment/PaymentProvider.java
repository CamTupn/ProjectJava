package com.scentstation.payment;

import com.scentstation.contract.PaymentNotice;
import com.scentstation.dto.request.MockPaymentCallbackRequest;

public interface PaymentProvider {
    String name();
    PaymentNotice verify(MockPaymentCallbackRequest request);
}
