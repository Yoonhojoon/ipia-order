package com.ipia.order.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주문 결제 완료 이벤트
 * 
 * Order 도메인에서 주문이 결제 완료로 상태 변경 시 발행하는 이벤트
 */
@Getter
@AllArgsConstructor
public class OrderPaidEvent {
    
    /**
     * 주문 ID
     */
    private final Long orderId;
    
    /**
     * 결제 완료 금액
     */
    private final Long paidAmount;
    
    /**
     * 결제 완료 일시
     */
    private final LocalDateTime paidAt;
    
    /**
     * 주문 결제 완료 이벤트 생성
     * 
     * @param orderId 주문 ID
     * @param paidAmount 결제 완료 금액
     * @return 주문 결제 완료 이벤트
     */
    public static OrderPaidEvent of(Long orderId, Long paidAmount) {
        return new OrderPaidEvent(orderId, paidAmount, LocalDateTime.now());
    }
}
