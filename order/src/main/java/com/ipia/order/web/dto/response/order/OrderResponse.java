package com.ipia.order.web.dto.response.order;

import com.ipia.order.order.domain.Order;
import com.ipia.order.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주문 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /**
     * 주문 ID
     */
    private Long id;

    /**
     * 회원 ID
     */
    private Long memberId;

    /**
     * 주문 총액
     */
    private Long totalAmount;

    /**
     * 주문 상태
     */
    private OrderStatus status;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    private LocalDateTime updatedAt;

    /**
     * Order 엔티티로부터 OrderResponse 생성
     */
    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .memberId(order.getMemberId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
