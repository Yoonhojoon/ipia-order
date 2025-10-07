package com.ipia.order.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주문 생성 이벤트
 * 
 * Order 도메인에서 주문 생성 시 발행하는 이벤트
 */
@Getter
@AllArgsConstructor
public class OrderCreatedEvent {
    
    /**
     * 주문 ID
     */
    private final Long orderId;
    
    /**
     * 회원 ID
     */
    private final Long memberId;
    
    /**
     * 주문 총액
     */
    private final Long totalAmount;
    
    /**
     * 주문 생성 일시
     */
    private final LocalDateTime createdAt;
    
    /**
     * 주문 생성 이벤트 생성
     * 
     * @param orderId 주문 ID
     * @param memberId 회원 ID
     * @param totalAmount 주문 총액
     * @return 주문 생성 이벤트
     */
    public static OrderCreatedEvent of(Long orderId, Long memberId, Long totalAmount) {
        return new OrderCreatedEvent(orderId, memberId, totalAmount, LocalDateTime.now());
    }
}
