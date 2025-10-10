package com.ipia.order.payment.intent.service;

import java.math.BigDecimal;

public interface PaymentIntentService {

    void store(String intentId, Long orderId, BigDecimal amount, String successUrl, String failUrl, String idempotencyKey, long ttlSeconds);

    PaymentIntentData get(String intentId);

    void delete(String intentId);

    record PaymentIntentData(
            String intentId,
            Long orderId,
            BigDecimal amount,
            String successUrl,
            String failUrl,
            String idempotencyKey
    ) {}
}

