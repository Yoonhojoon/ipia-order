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
    private OrderStatus status = OrderStatus.PENDING;

    @Builder
    private Order(Long memberId, Long totalAmount) {
        this.memberId = memberId;
        this.totalAmount = totalAmount;
    }

    public static Order create(Long memberId, Long totalAmount) {
        return Order.builder()
                .memberId(memberId)
                .totalAmount(totalAmount)
                .build();
    }

    public void transitionToPaid() {
        this.status = OrderStatus.PAID;
    }

    public void transitionToCanceled() {
        this.status = OrderStatus.CANCELED;
    }

    public void transitionToCompleted() {
        this.status = OrderStatus.COMPLETED;
    }

}
