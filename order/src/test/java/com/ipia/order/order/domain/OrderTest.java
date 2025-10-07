package com.ipia.order.order.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ipia.order.common.exception.order.OrderHandler;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;

class OrderTest {

    @Test
    @DisplayName("주문 생성 시 음수 금액이면 예외가 발생한다")
    void createOrderWithNegativeAmount() {
        // given
        Long memberId = 1L;
        Long negativeAmount = -1000L;

        // when & then
        assertThatThrownBy(() -> Order.create(memberId, negativeAmount))
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.NEGATIVE_ORDER_AMOUNT);
    }

    @Test
    @DisplayName("주문 생성 시 0원이면 예외가 발생한다")
    void createOrderWithZeroAmount() {
        // given
        Long memberId = 1L;
        Long zeroAmount = 0L;

        // when & then
        assertThatThrownBy(() -> Order.create(memberId, zeroAmount))
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_ORDER_AMOUNT);
    }

    @Test
    @DisplayName("주문 생성 시 회원 ID가 null이면 예외가 발생한다")
    void createOrderWithNullMemberId() {
        // given
        Long memberId = null;
        Long amount = 1000L;

        // when & then
        assertThatThrownBy(() -> Order.create(memberId, amount))
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.MEMBER_ID_REQUIRED);
    }

    @Test
    @DisplayName("이미 결제된 주문에서 다시 결제 완료로 전이하면 예외가 발생한다")
    void transitionToPaidFromAlreadyPaidOrder() {
        // given
        Order order = Order.create(1L, 1000L);
        order.transitionToPending(); // PENDING 상태로 전이
        order.transitionToPaid(); // 이미 결제 완료 상태

        // when & then
        assertThatThrownBy(() -> order.transitionToPaid())
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_TRANSITION_TO_PAID);
    }

    @Test
    @DisplayName("이미 취소된 주문에서 결제 완료로 전이하면 예외가 발생한다")
    void transitionToPaidFromCanceledOrder() {
        // given
        Order order = Order.create(1L, 1000L);
        order.transitionToCanceled(); // 취소 상태

        // when & then
        assertThatThrownBy(() -> order.transitionToPaid())
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_TRANSITION_TO_PAID);
    }

    @Test
    @DisplayName("이미 완료된 주문에서 결제 완료로 전이하면 예외가 발생한다")
    void transitionToPaidFromCompletedOrder() {
        // given
        Order order = Order.create(1L, 1000L);
        order.transitionToPending(); // PENDING 상태로 전이
        order.transitionToPaid();
        order.transitionToCompleted(); // 완료 상태

        // when & then
        assertThatThrownBy(() -> order.transitionToPaid())
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_TRANSITION_TO_PAID);
    }

    @Test
    @DisplayName("이미 취소된 주문에서 다시 취소로 전이하면 예외가 발생한다")
    void transitionToCanceledFromAlreadyCanceledOrder() {
        // given
        Order order = Order.create(1L, 1000L);
        order.transitionToCanceled(); // 이미 취소 상태

        // when & then
        assertThatThrownBy(() -> order.transitionToCanceled())
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_TRANSITION_TO_CANCELED);
    }

    @Test
    @DisplayName("이미 완료된 주문에서 취소로 전이하면 예외가 발생한다")
    void transitionToCanceledFromCompletedOrder() {
        // given
        Order order = Order.create(1L, 1000L);
        order.transitionToPending(); // PENDING 상태로 전이
        order.transitionToPaid();
        order.transitionToCompleted(); // 완료 상태

        // when & then
        assertThatThrownBy(() -> order.transitionToCanceled())
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_TRANSITION_TO_CANCELED);
    }

    @Test
    @DisplayName("이미 결제된 주문에서 취소로 전이하면 예외가 발생한다")
    void transitionToCanceledFromPaidOrder() {
        // given
        Order order = Order.create(1L, 1000L);
        order.transitionToPending(); // PENDING 상태로 전이
        order.transitionToPaid(); // 결제 완료 상태

        // when & then
        assertThatThrownBy(() -> order.transitionToCanceled())
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_TRANSITION_TO_CANCELED);
    }
}
