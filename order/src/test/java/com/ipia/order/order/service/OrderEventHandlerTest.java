package com.ipia.order.order.service;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ipia.order.order.event.PaymentApprovedEvent;
import com.ipia.order.order.event.PaymentCanceledEvent;

/**
 * OrderEventHandler 테스트
 * 
 * Payment 도메인 이벤트를 구독하여 Order 도메인 처리하는 핸들러 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventHandler 테스트")
class OrderEventHandlerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderEventHandler orderEventHandler;

    @Test
    @DisplayName("PaymentApprovedEvent 수신 시 handlePaymentApproved 호출")
    void handlePaymentApproved_CallsOrderService() {
        // given
        PaymentApprovedEvent event = PaymentApprovedEvent.of(
            1L, 
            BigDecimal.valueOf(10000), 
            "txn-123"
        );

        // when
        orderEventHandler.handlePaymentApproved(event);

        // then
        verify(orderService).handlePaymentApproved(event.getOrderId());
    }

    @Test
    @DisplayName("PaymentCanceledEvent 수신 시 handlePaymentCanceled 호출")
    void handlePaymentCanceled_CallsOrderService() {
        // given
        PaymentCanceledEvent event = PaymentCanceledEvent.of(
            1L, 
            BigDecimal.valueOf(10000), 
            "취소 사유", 
            "txn-123"
        );

        // when
        orderEventHandler.handlePaymentCanceled(event);

        // then
        verify(orderService).handlePaymentCanceled(event.getOrderId());
    }
}
