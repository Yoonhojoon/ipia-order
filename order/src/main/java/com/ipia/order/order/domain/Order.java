package com.ipia.order.order.domain;

import com.ipia.order.common.entity.BaseEntity;
import com.ipia.order.common.exception.order.OrderHandler;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;
import com.ipia.order.order.enums.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Builder(access = AccessLevel.PROTECTED)
    private Order(Long memberId, Long totalAmount) {
        validateMemberId(memberId);
        validateTotalAmount(totalAmount);
        this.memberId = memberId;
        this.totalAmount = totalAmount;
    }

    /**
     * 멱등성 서비스에서 사용하는 팩토리 메서드
     * 캐시된 데이터로부터 Order 객체를 복원할 때 사용
     */
    public static Order restore(Long id, Long memberId, Long totalAmount, OrderStatus status) {
        Order order = new Order();
        order.id = id;
        order.memberId = memberId;
        order.totalAmount = totalAmount;
        order.status = status != null ? status : OrderStatus.CREATED;
        return order;
    }

    public static Order create(Long memberId, Long totalAmount) {
        return Order.builder()
                .memberId(memberId)
                .totalAmount(totalAmount)
                .build();
    }

    // ==================== 내부 상태 변환 메서드 ====================

    public void confirm() {
        validateTransition(OrderStatus.CREATED, OrderErrorStatus.INVALID_ORDER_STATE);
        this.status = OrderStatus.CONFIRMED;
    }

    public void startFulfillment() {
        validateTransition(OrderStatus.CONFIRMED, OrderErrorStatus.INVALID_ORDER_STATE);
        this.status = OrderStatus.FULFILLMENT_STARTED;
    }

    public void ship() {
        validateTransition(OrderStatus.FULFILLMENT_STARTED, OrderErrorStatus.INVALID_ORDER_STATE);
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        validateTransition(OrderStatus.SHIPPED, OrderErrorStatus.INVALID_ORDER_STATE);
        this.status = OrderStatus.DELIVERED;
    }

    public void complete() {
        // 완료는 DELIVERED 이후만 허용 (결제 미도입 단계 기준)
        validateTransition(OrderStatus.DELIVERED, OrderErrorStatus.INVALID_TRANSITION_TO_COMPLETED);
        this.status = OrderStatus.COMPLETED;
    }

    public void requestCancel() {
        // 생성/확정 단계에서만 취소 요청 허용
        if (!(this.status == OrderStatus.CREATED || this.status == OrderStatus.CONFIRMED)) {
            throw new OrderHandler(OrderErrorStatus.INVALID_TRANSITION_TO_CANCELED);
        }
        this.status = OrderStatus.CANCEL_REQUESTED;
    }

    public void cancel() {
        // CANCEL_REQUESTED 또는 CREATED/CONFIRMED 에서만 최종 취소 허용
        if (!(this.status == OrderStatus.CANCEL_REQUESTED || this.status == OrderStatus.CREATED || this.status == OrderStatus.CONFIRMED)) {
            throw new OrderHandler(OrderErrorStatus.INVALID_TRANSITION_TO_CANCELED);
        }
        this.status = OrderStatus.CANCELED;
    }

    // 레거시 결제 중심 전이 메서드 제거됨

    private void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new OrderHandler(OrderErrorStatus.MEMBER_ID_REQUIRED);
        }
    }

    private void validateTotalAmount(Long totalAmount) {
        if (totalAmount == null || totalAmount <= 0) {
            throw new OrderHandler(OrderErrorStatus.INVALID_ORDER_AMOUNT);
        }
    }

    private void validateTransition(OrderStatus expectedStatus, OrderErrorStatus errorStatus) {
        if (this.status != expectedStatus) {
            throw new OrderHandler(errorStatus);
        }
    }

    // 레거시 취소 검증 제거됨

    /**
     * 테스트용 Order 엔티티 생성 메서드
     */
    public static Order createTestOrder(Long id, Long memberId, Long totalAmount, OrderStatus status) {
        Order order = Order.builder()
                .memberId(memberId)
                .totalAmount(totalAmount)
                .build();
        // Reflection을 사용하여 ID와 상태 설정 (테스트용)
        try {
            java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, id);

            java.lang.reflect.Field statusField = Order.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(order, status);
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Order 생성 실패", e);
        }
        return order;
    }
}
