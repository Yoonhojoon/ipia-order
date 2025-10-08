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
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_ORDER_AMOUNT);
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
    @DisplayName("CONFIRMED 상태에서 다시 confirm() 시도하면 예외가 발생한다")
    void confirmTwiceThrows() {
        // given
        Order order = Order.create(1L, 1000L);
        order.confirm();

        // when & then
        assertThatThrownBy(order::confirm)
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_ORDER_STATE);
    }

    @Test
    @DisplayName("CREATED 상태에서 startFulfillment() 시도하면 예외가 발생한다")
    void startFulfillmentFromCreatedThrows() {
        // given
        Order order = Order.create(1L, 1000L);

        // when & then
        assertThatThrownBy(order::startFulfillment)
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_ORDER_STATE);
    }

    @Test
    @DisplayName("DELIVERED 전 complete() 호출 시 예외가 발생한다")
    void completeBeforeDeliveredThrows() {
        // given
        Order order = Order.create(1L, 1000L);
        order.confirm();
        order.startFulfillment();
        order.ship();

        // when & then
        assertThatThrownBy(order::complete)
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_TRANSITION_TO_COMPLETED);
    }

    @Test
    @DisplayName("SHIPPED 상태에서 requestCancel() 시 예외가 발생한다")
    void requestCancelFromShippedThrows() {
        // given
        Order order = Order.create(1L, 1000L);
        order.confirm();
        order.startFulfillment();
        order.ship();

        // when & then
        assertThatThrownBy(order::requestCancel)
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_TRANSITION_TO_CANCELED);
    }

    @Test
    @DisplayName("SHIPPED 상태에서 cancel() 시 예외가 발생한다")
    void cancelFromShippedThrows() {
        // given
        Order order = Order.create(1L, 1000L);
        order.confirm();
        order.startFulfillment();
        order.ship();

        // when & then
        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_TRANSITION_TO_CANCELED);
    }

    @Test
    @DisplayName("이미 취소 완료된 주문에서 cancel() 재호출 시 예외가 발생한다")
    void cancelTwiceThrows() {
        // given
        Order order = Order.create(1L, 1000L);
        order.requestCancel();
        order.cancel();

        // when & then
        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderHandler.class)
                .hasFieldOrPropertyWithValue("status", OrderErrorStatus.INVALID_TRANSITION_TO_CANCELED);
    }
}
