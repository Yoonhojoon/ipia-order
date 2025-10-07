package com.ipia.order.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 취소 이벤트
 * 
 * 결제 도메인에서 주문 도메인으로 발행하는 이벤트
 */
@Getter
@AllArgsConstructor
public class PaymentCanceledEvent {
    
    /**
     * 주문 ID
     */
    private final Long orderId;
    
    /**
     * 취소 금액
     */
    private final BigDecimal canceledAmount;
    
    /**
     * 취소 일시
     */
    private final LocalDateTime canceledAt;
    
    /**
     * 취소 사유 (선택사항)
     */
    private final String reason;
    
    /**
     * 결제 제공업체 거래 ID (선택사항)
     */
    private final String providerTransactionId;
    
    /**
     * 결제 취소 이벤트 생성
     * 
     * @param orderId 주문 ID
     * @param canceledAmount 취소 금액
     * @param reason 취소 사유
     * @param providerTransactionId 결제 제공업체 거래 ID
     * @return 결제 취소 이벤트
     */
    public static PaymentCanceledEvent of(Long orderId, BigDecimal canceledAmount, String reason, String providerTransactionId) {
        return new PaymentCanceledEvent(orderId, canceledAmount, LocalDateTime.now(), reason, providerTransactionId);
    }
}
