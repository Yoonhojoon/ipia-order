package com.ipia.order.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주문 취소 이벤트
 * 
 * Order 도메인에서 주문 취소 시 발행하는 이벤트
 */
@Getter
@AllArgsConstructor
public class OrderCanceledEvent {
    
    /**
     * 주문 ID
     */
    private final Long orderId;
    
    /**
     * 취소 사유
     */
    private final String reason;
    
    /**
     * 취소 일시
     */
    private final LocalDateTime canceledAt;
    
    /**
     * 주문 취소 이벤트 생성
     * 
     * @param orderId 주문 ID
     * @param reason 취소 사유
     * @return 주문 취소 이벤트
     */
    public static OrderCanceledEvent of(Long orderId, String reason) {
        return new OrderCanceledEvent(orderId, reason, LocalDateTime.now());
    }
}
