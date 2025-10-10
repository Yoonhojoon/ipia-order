package com.ipia.order.payment.service.external;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

/**
 * 기본 구현체: 실제 연동 전까지는 단순 에코 응답을 반환한다.
 * 운영 연동 시 이 구현을 교체하거나 내부 로직을 실제 API 호출로 대체한다.
 */
@Component
public class TossPaymentClientImpl implements TossPaymentClient {

    @Override
    public TossConfirmResponse confirm(String paymentKey, String orderId, BigDecimal amount) {
        // 단순 에코 형태의 더미 응답
        return new TossConfirmResponse(paymentKey, orderId, amount);
    }

    @Override
    public TossCancelResponse cancel(String paymentKey, BigDecimal cancelAmount, String reason) {
        // 단순 에코 형태의 더미 응답
        return new TossCancelResponse(paymentKey, cancelAmount, "CANCELED");
    }
}


