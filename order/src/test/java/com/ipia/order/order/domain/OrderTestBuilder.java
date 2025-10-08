package com.ipia.order.order.domain;

import com.ipia.order.order.enums.OrderStatus;

import java.time.LocalDateTime;

/**
 * 테스트용 Order 객체 생성을 위한 빌더 클래스
 * 테스트에서만 사용되며 프로덕션 코드에는 영향을 주지 않음
 */
public class OrderTestBuilder {

    private Long id;
    private Long memberId;
    private Long totalAmount;
    private OrderStatus status = OrderStatus.CREATED;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private OrderTestBuilder() {}

    public static OrderTestBuilder builder() {
        return new OrderTestBuilder();
    }

    public OrderTestBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public OrderTestBuilder memberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }

    public OrderTestBuilder totalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public OrderTestBuilder status(OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderTestBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public OrderTestBuilder updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public Order build() {
        if (id == null) {
            // 일반적인 Order 생성
            return Order.create(memberId, totalAmount);
        } else {
            // ID가 있는 경우 복원 메서드 사용
            Order order = Order.restore(id, memberId, totalAmount, status);
            
            // 테스트용으로 BaseEntity 필드 설정 (Reflection 사용)
            if (createdAt != null || updatedAt != null) {
                setBaseEntityFields(order, createdAt, updatedAt);
            }
            
            return order;
        }
    }

    private void setBaseEntityFields(Order order, LocalDateTime createdAt, LocalDateTime updatedAt) {
        try {
            if (createdAt != null) {
                java.lang.reflect.Field createdAtField = order.getClass().getSuperclass().getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(order, createdAt);
            }
            
            if (updatedAt != null) {
                java.lang.reflect.Field updatedAtField = order.getClass().getSuperclass().getDeclaredField("updatedAt");
                updatedAtField.setAccessible(true);
                updatedAtField.set(order, updatedAt);
            }
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Order 생성 실패", e);
        }
    }
}
