package com.ipia.order.order.service;

import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipia.order.common.exception.order.OrderHandler;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.order.domain.Order;
import com.ipia.order.order.enums.OrderStatus;
import com.ipia.order.order.event.OrderCanceledEvent;
import com.ipia.order.order.event.OrderCreatedEvent;
import com.ipia.order.order.event.OrderPaidEvent;
import com.ipia.order.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

/**
 * 주문 서비스 구현체
 * 
 * TODO: TDD Green 단계에서 구현 예정
 * 현재는 테스트 컴파일을 위한 스텁 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MemberService memberService;
    private final ApplicationEventPublisher eventPublisher;
    // TODO: IdempotencyKeyService 의존성 추가 (Phase 2 후반에 구현)

    @Override
    @Transactional
    public Order createOrder(long memberId, long totalAmount, String idempotencyKey) {
        // TODO: TDD Green 단계에서 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Order> getOrder(long orderId) {
        // TODO: TDD Green 단계에서 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Order> listOrders(Long memberId, OrderStatus status, int page, int size) {
        // TODO: TDD Green 단계에서 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public Order cancelOrder(long orderId, String reason) {
        // TODO: TDD Green 단계에서 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public void handlePaymentApproved(long orderId) {
        // TODO: TDD Green 단계에서 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public void handlePaymentCanceled(long orderId) {
        // TODO: TDD Green 단계에서 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
