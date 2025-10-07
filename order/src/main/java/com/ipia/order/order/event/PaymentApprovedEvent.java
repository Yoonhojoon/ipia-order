package com.ipia.order.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 승인 이벤트
 * 
 * 결제 도메인에서 주문 도메인으로 발행하는 이벤트
 */
@Getter
@AllArgsConstructor
public class PaymentApprovedEvent {
    
    /**
     * 주문 ID
     */
    private final Long orderId;
    
    /**
     * 결제 승인 금액
     */
    private final BigDecimal paidAmount;
    
    /**
     * 결제 승인 일시
     */
    private final LocalDateTime approvedAt;
    
    /**
     * 결제 제공업체 거래 ID (선택사항)
     */
    private final String providerTransactionId;
    
    /**
     * 결제 승인 이벤트 생성
     * 
     * @param orderId 주문 ID
     * @param paidAmount 결제 승인 금액
     * @param providerTransactionId 결제 제공업체 거래 ID
     * @return 결제 승인 이벤트
     */
    public static PaymentApprovedEvent of(Long orderId, BigDecimal paidAmount, String providerTransactionId) {
        return new PaymentApprovedEvent(orderId, paidAmount, LocalDateTime.now(), providerTransactionId);
    }
}
