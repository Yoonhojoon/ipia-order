package com.ipia.order.order.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipia.order.common.exception.order.OrderHandler;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;
import com.ipia.order.idempotency.service.IdempotencyKeyService;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.order.domain.Order;
import com.ipia.order.order.enums.OrderStatus;
import com.ipia.order.order.event.OrderCanceledEvent;
import com.ipia.order.order.event.OrderCreatedEvent;
import com.ipia.order.order.event.OrderPaidEvent;
import com.ipia.order.order.repository.OrderRepository;
import com.ipia.order.web.dto.response.order.OrderListResponse;
import com.ipia.order.web.dto.response.order.OrderResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 서비스 구현체
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    // ==================== Constants ====================
    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final String CREATE_ORDER_ENDPOINT = "POST /api/orders";

    // ==================== Dependencies ====================
    private final OrderRepository orderRepository;
    private final MemberService memberService; // createOrder 등 다른 메서드에서 사용 예정
    private final ApplicationEventPublisher eventPublisher;
    private final IdempotencyKeyService idempotencyKeyService;

    @Override
    @Transactional
    public Order createOrder(long memberId, long totalAmount, @Nullable String idempotencyKey) {
        log.info("[Order] 주문 생성 요청: memberId={}, amount={}, idemKey={}", memberId, totalAmount, idempotencyKey);
        validateMemberForOrder(memberId);
        validateOrderAmount(totalAmount);
        Supplier<Order> operation = () -> {
            Order order = Order.create(memberId, totalAmount);
            // 신규 흐름: 생성 직후는 CREATED → confirm을 별도 단계로 유지
            Order saved = orderRepository.save(order);
            eventPublisher.publishEvent(OrderCreatedEvent.of(saved.getId(), memberId, totalAmount));
            log.info("[Order] 주문 생성 이벤트 발행: orderId={}, memberId={}, amount={}", saved.getId(), memberId, totalAmount);
            return saved;
        };

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            Order created = operation.get();
            log.info("[Order] 주문 생성 완료(멱등키 없음): orderId={}", created.getId());
            return created;
        }
        Order created = idempotencyKeyService.executeWithIdempotency(CREATE_ORDER_ENDPOINT, idempotencyKey, Order.class, operation);
        log.info("[Order] 주문 생성 완료(멱등키 적용): orderId={}, idemKey={}", created.getId(), idempotencyKey);
        return created;
    }

    @Override
    public Optional<Order> getOrder(long orderId) {
        log.info("[Order] 주문 단건 조회 요청: orderId={}", orderId);
        findOrderById(orderId); // 주문 존재 여부만 확인
        throw new OrderHandler(OrderErrorStatus.ACCESS_DENIED);
    }

    @Override
    public OrderListResponse listOrders(@Nullable Long memberId, @Nullable String status, int page, int size) {
        log.info("[Order] 주문 목록 조회 요청: memberId={}, status={}, page={}, size={}", memberId, status, page, size);
        validatePagination(page, size);
        validateMemberFilter(memberId);

        // String status를 OrderStatus enum으로 변환
        OrderStatus orderStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[Order] 잘못된 상태값 필터: status={}", status);
                throw new OrderHandler(OrderErrorStatus.INVALID_FILTER);
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;
        
        // 동적 쿼리 로직: 필터 조건에 따라 다른 Repository 메서드 호출
        if (memberId != null && orderStatus != null) {
            // 회원 ID와 상태 모두 지정
            orderPage = orderRepository.findByMemberIdAndStatus(memberId, orderStatus, pageable);
        } else if (memberId != null) {
            // 회원 ID만 지정
            orderPage = orderRepository.findByMemberId(memberId, pageable);
        } else if (orderStatus != null) {
            // 상태만 지정
            orderPage = orderRepository.findByStatus(orderStatus, pageable);
        } else {
            // 필터 없음 - 전체 조회
            orderPage = orderRepository.findAll(pageable);
        }

        // DTO 매핑
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(OrderResponse::from)
                .toList();
        
        OrderListResponse response = OrderListResponse.builder()
                .orders(orderResponses)
                .totalCount(orderPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(orderPage.getTotalPages())
                .build();
        log.info("[Order] 주문 목록 조회 성공: count={}, totalPages={}", response.getOrders().size(), response.getTotalPages());
        return response;
    }

    @Override
    @Transactional
    public Order cancelOrder(long orderId, @Nullable String reason) {
        log.info("[Order] 주문 취소 요청: orderId={}, reason={}", orderId, reason);
        Order order = findOrderById(orderId);

        // 이미 취소된 주문
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new OrderHandler(OrderErrorStatus.ALREADY_CANCELED);
        }

        // 배송 이후에는 취소 불가
        if (order.getStatus() == OrderStatus.SHIPPED
                || order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.COMPLETED) {
            throw new OrderHandler(OrderErrorStatus.INVALID_ORDER_STATE);
        }

        // CANCEL_REQUESTED 또는 CREATED/CONFIRMED 에서는 취소 허용
        if (order.getStatus() == OrderStatus.CANCEL_REQUESTED
                || order.getStatus() == OrderStatus.CREATED
                || order.getStatus() == OrderStatus.CONFIRMED) {
            order.cancel();
            Order saved = orderRepository.save(order);
            eventPublisher.publishEvent(OrderCanceledEvent.of(saved.getId(), reason));
            log.info("[Order] 주문 취소 성공: orderId={}", saved.getId());
            return saved;
        }

        throw new OrderHandler(OrderErrorStatus.INVALID_ORDER_STATE);
    }

    @Override
    @Transactional
    public void handlePaymentApproved(long orderId) {
        log.info("[Order] 결제 승인 처리 요청: orderId={}", orderId);
        Order order = findOrderById(orderId);
        // 승인 시 CREATED -> CONFIRMED 만 허용
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new OrderHandler(OrderErrorStatus.INVALID_ORDER_STATE);
        }
        order.confirm();
        orderRepository.save(order);
        eventPublisher.publishEvent(OrderPaidEvent.of(order.getId(), order.getTotalAmount()));
        log.info("[Order] 결제 승인 처리 완료(확정): orderId={}", order.getId());
    }

    @Override
    @Transactional
    public void handlePaymentCanceled(long orderId) {
        log.info("[Order] 결제 취소 처리 요청: orderId={}", orderId);
        Order order = findOrderById(orderId);
        // 승인 이후(=CONFIRMED)만 결제 취소 허용, 배송 이후는 불가
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new OrderHandler(OrderErrorStatus.INVALID_ORDER_STATE);
        }
        order.cancel();
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(OrderCanceledEvent.of(saved.getId(), null));
        log.info("[Order] 결제 취소 처리 완료: orderId={}", saved.getId());
    }

    // ==================== Private Helper Methods ====================

    /**
     * 주문 ID로 주문을 조회합니다.
     * 주문이 존재하지 않으면 ORDER_NOT_FOUND 예외를 발생시킵니다.
     */
    private Order findOrderById(long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderHandler(OrderErrorStatus.ORDER_NOT_FOUND));
    }

    /**
     * 주문 생성을 위한 회원 검증을 수행합니다.
     */
    private void validateMemberForOrder(long memberId) {
        Optional<Member> optionalMember = memberService.findById(memberId);
        if (optionalMember.isEmpty()) {
            throw new OrderHandler(OrderErrorStatus.MEMBER_NOT_FOUND);
        }
        Member member = optionalMember.get();
        if (!member.isActive()) {
            throw new OrderHandler(OrderErrorStatus.INACTIVE_MEMBER);
        }
    }

    /**
     * 주문 금액의 유효성을 검증합니다.
     */
    private void validateOrderAmount(long totalAmount) {
        if (totalAmount <= 0) {
            throw new OrderHandler(OrderErrorStatus.INVALID_AMOUNT);
        }
    }

    /**
     * 페이지네이션 파라미터의 유효성을 검증합니다.
     */
    private void validatePagination(int page, int size) {
        if (page < MIN_PAGE || size < MIN_SIZE) {
            throw new OrderHandler(OrderErrorStatus.INVALID_PAGINATION);
        }
    }

    /**
     * 회원 필터의 유효성을 검증합니다.
     */
    private void validateMemberFilter(@Nullable Long memberId) {
        // 필터 검증: memberId가 주어졌는데 존재하지 않으면 INVALID_FILTER
        if (memberId != null) {
            Optional<Member> memberOpt = memberService.findById(memberId);
            if (memberOpt.isEmpty()) {
                throw new OrderHandler(OrderErrorStatus.INVALID_FILTER);
            }
        }
    }
}