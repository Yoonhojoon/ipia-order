package com.ipia.order.payment.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.ipia.order.payment.service.external.TossPaymentClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TossPaymentClient tossPaymentClient;

    @Override
    public String createIntent(long orderId, BigDecimal amount, String successUrl, String failUrl, String idempotencyKey) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void verify(String intentId, String paymentKey, long orderId, BigDecimal amount) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Long approve(String intentId, String paymentKey, long orderId, BigDecimal amount) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void cancel(String paymentKey, BigDecimal cancelAmount, String reason) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}


