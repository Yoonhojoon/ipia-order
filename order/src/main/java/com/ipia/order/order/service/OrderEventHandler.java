package com.ipia.order.order.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ipia.order.order.event.PaymentApprovedEvent;
import com.ipia.order.order.event.PaymentCanceledEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Order 도메인 이벤트 핸들러
 * 
 * Payment 도메인에서 발행하는 이벤트를 구독하여 Order 도메인 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventHandler {
    
    private final OrderService orderService;
    
    /**
     * 결제 승인 이벤트 처리
     * 
     * @param event 결제 승인 이벤트
     */
    @EventListener
    @Transactional
    public void handlePaymentApproved(PaymentApprovedEvent event) {
        log.info("Payment approved for order: {}", event.getOrderId());
        orderService.handlePaymentApproved(event.getOrderId());
    }
    
    /**
     * 결제 취소 이벤트 처리
     * 
     * @param event 결제 취소 이벤트
     */
    @EventListener
    @Transactional
    public void handlePaymentCanceled(PaymentCanceledEvent event) {
        log.info("Payment canceled for order: {}", event.getOrderId());
        orderService.handlePaymentCanceled(event.getOrderId());
    }
}
