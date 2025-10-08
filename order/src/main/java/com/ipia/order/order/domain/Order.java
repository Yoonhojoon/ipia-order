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

    public void transitionToPending() {
        validateTransition(OrderStatus.CREATED, OrderErrorStatus.INVALID_TRANSITION_TO_PENDING);
        this.status = OrderStatus.PENDING;
    }

    public void transitionToPaid() {
        validateTransition(OrderStatus.PENDING, OrderErrorStatus.INVALID_TRANSITION_TO_PAID);
        this.status = OrderStatus.PAID;
    }

    public void transitionToCanceled() {
        validateCancellation();
        this.status = OrderStatus.CANCELED;
    }

    public void transitionToCompleted() {
        validateTransition(OrderStatus.PAID, OrderErrorStatus.INVALID_TRANSITION_TO_COMPLETED);
        this.status = OrderStatus.COMPLETED;
    }

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

    private void validateCancellation() {
        if (!isCancellable()) {
            throw new OrderHandler(OrderErrorStatus.INVALID_TRANSITION_TO_CANCELED);
        }
    }

    private boolean isCancellable() {
        return this.status == OrderStatus.CREATED || this.status == OrderStatus.PENDING;
    }

}
