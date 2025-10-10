package com.ipia.order.payment.service;

import java.math.BigDecimal;

/**
 * 결제 서비스 응용 계층 인터페이스.
 * 단일 서비스로 시작하고, 외부 연동/저장소는 포트로 분리한다.
 */
public interface PaymentService {

    String createIntent(long orderId, BigDecimal amount, String successUrl, String failUrl);

    void verify(String intentId, String paymentKey, long orderId, BigDecimal amount, String idempotencyKey);

    Long approve(String intentId, String paymentKey, long orderId, BigDecimal amount, String idempotencyKey);

    void cancel(String paymentKey, BigDecimal cancelAmount, String reason);
}


