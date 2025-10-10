package com.ipia.order.payment.service.port;

import java.math.BigDecimal;

/**
 * PaymentIntent 임시 저장소 포트
 * Redis 기반으로 결제 의도를 임시 저장하고 TTL로 자동 만료
 */
public interface PaymentIntentStore {

    /**
     * 결제 의도 저장
     * 
     * @param intentId 의도 ID
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @param successUrl 성공 URL
     * @param failUrl 실패 URL
     * @param idempotencyKey 멱등성 키
     * @param ttlSeconds TTL (초)
     */
    void store(String intentId, Long orderId, BigDecimal amount, String successUrl, String failUrl, String idempotencyKey, long ttlSeconds);

    /**
     * 결제 의도 조회
     * 
     * @param intentId 의도 ID
     * @return 저장된 의도 정보 또는 null
     */
    PaymentIntentData get(String intentId);

    /**
     * 결제 의도 삭제
     * 
     * @param intentId 의도 ID
     */
    void delete(String intentId);

    /**
     * 결제 의도 데이터 클래스
     */
    record PaymentIntentData(
            String intentId,
            Long orderId,
            BigDecimal amount,
            String successUrl,
            String failUrl,
            String idempotencyKey
    ) {}
}
