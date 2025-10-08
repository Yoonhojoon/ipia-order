package com.ipia.order.order.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import com.ipia.order.idempotency.service.IdempotencyKeyService;

import com.ipia.order.member.domain.Member;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.order.domain.Order;
import com.ipia.order.order.domain.OrderTestBuilder;
import com.ipia.order.order.enums.OrderStatus;
import com.ipia.order.order.event.OrderCreatedEvent;
import com.ipia.order.order.event.OrderPaidEvent;
import com.ipia.order.order.repository.OrderRepository;
import com.ipia.order.common.exception.order.OrderHandler;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Collections;

/**
 * OrderService 실패 케이스 테스트
 * 
 * TDD Red 단계: 모든 테스트는 먼저 실패해야 함
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 실패 케이스 테스트")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private MemberService memberService;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @Mock
    private IdempotencyKeyService idempotencyKeyService;
    
    @InjectMocks
    private OrderServiceImpl orderService;

    private Member validMember;
    private Order validOrder;

    @BeforeEach
    void setUp() {
        validMember = Member.createTestMember(1L, "홍길동", "hong@example.com", "encodedPassword123!", null);

        validOrder = OrderTestBuilder.builder()
                .id(1L)
                .memberId(1L)
                .totalAmount(10000L)
                .status(OrderStatus.CREATED)
                .build();

        // 기본 동작: 멱등 키가 주어지면 공급자(operation)를 실행해 결과를 반환
        lenient().when(idempotencyKeyService.executeWithIdempotency(anyString(), anyString(), any(Class.class), any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> op = invocation.getArgument(3);
                    return op.get();
                });
    }

    @Nested
    @DisplayName("createOrder")
    class CreateOrderTest {

        // 실패 케이스
        @Test
        @DisplayName("존재하지 않는 회원으로 주문 생성 시 MemberNotFoundException 발생")
        void createOrder_WithNonExistentMember_ThrowsMemberNotFoundException() {
            // given
            long nonExistentMemberId = 999L;
            long totalAmount = 10000L;
            String idempotencyKey = "test-key";

            given(memberService.findById(nonExistentMemberId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(nonExistentMemberId, totalAmount, idempotencyKey))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.MEMBER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("비활성 회원으로 주문 생성 시 InactiveMemberException 발생")
        void createOrder_WithInactiveMember_ThrowsInactiveMemberException() {
            // given
            long memberId = 1L;
            long totalAmount = 10000L;
            String idempotencyKey = "test-key";

            Member inactiveMember = Member.createTestMember(memberId, "홍길동", "hong@example.com", "encodedPassword123!", null);
            // 비활성 회원으로 설정
            try {
                Field isActiveField = Member.class.getDeclaredField("isActive");
                isActiveField.setAccessible(true);
                isActiveField.set(inactiveMember, false);
            } catch (Exception e) {
                throw new RuntimeException("비활성 회원 설정 실패", e);
            }

            given(memberService.findById(memberId))
                    .willReturn(Optional.of(inactiveMember));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, totalAmount, idempotencyKey))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.INACTIVE_MEMBER.getCode());
        }

        @Test
        @DisplayName("음수 금액으로 주문 생성 시 InvalidAmountException 발생")
        void createOrder_WithNegativeAmount_ThrowsInvalidAmountException() {
            // given
            long memberId = 1L;
            long negativeAmount = -1000L;
            String idempotencyKey = "test-key";

            given(memberService.findById(memberId))
                    .willReturn(Optional.of(validMember));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, negativeAmount, idempotencyKey))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.INVALID_AMOUNT.getCode());
        }

        @Test
        @DisplayName("0원 금액으로 주문 생성 시 InvalidAmountException 발생")
        void createOrder_WithZeroAmount_ThrowsInvalidAmountException() {
            // given
            long memberId = 1L;
            long zeroAmount = 0L;
            String idempotencyKey = "test-key";

            given(memberService.findById(memberId))
                    .willReturn(Optional.of(validMember));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, zeroAmount, idempotencyKey))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.INVALID_AMOUNT.getCode());
        }

        @Test
        @DisplayName("중복된 멱등 키로 주문 생성 시 IdempotencyConflictException 발생")
        void createOrder_WithDuplicateIdempotencyKey_ThrowsIdempotencyConflictException() {
            // given
            long memberId = 1L;
            long totalAmount = 10000L;
            String duplicateKey = "duplicate-key";

            given(memberService.findById(memberId))
                    .willReturn(Optional.of(validMember));
            
            // 멱등 서비스가 중복 키 충돌을 유발하도록 설정
            given(idempotencyKeyService.executeWithIdempotency(eq("POST /api/orders"), eq(duplicateKey), any(Class.class), any()))
                    .willThrow(new OrderHandler(OrderErrorStatus.IDEMPOTENCY_CONFLICT));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(memberId, totalAmount, duplicateKey))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.IDEMPOTENCY_CONFLICT.getCode());
        }
    }

    @Nested
    @DisplayName("getOrder")
    class GetOrderTest {

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 OrderNotFoundException 발생")
        void getOrder_WithNonExistentOrder_ThrowsOrderNotFoundException() {
            // given
            long nonExistentOrderId = 999L;

            given(orderRepository.findById(nonExistentOrderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrder(nonExistentOrderId))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.ORDER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("권한 없는 주문 조회 시 AccessDeniedException 발생")
        void getOrder_WithUnauthorizedAccess_ThrowsAccessDeniedException() {
            // given
            long orderId = 1L;
            long unauthorizedMemberId = 2L; // 다른 회원

            Order order = OrderTestBuilder.builder()
                    .id(orderId)
                    .memberId(1L)
                    .totalAmount(10000L)
                    .status(OrderStatus.CREATED)
                    .build(); // 다른 회원의 주문

            given(orderRepository.findById(orderId))
                    .willReturn(Optional.of(order));

            // TODO: 현재 사용자 인증 정보 Mock 설정 필요
            // when & then
            assertThatThrownBy(() -> orderService.getOrder(orderId))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.ACCESS_DENIED.getCode());
        }
    }

    @Nested
    @DisplayName("listOrders")
    class ListOrdersTest {

        // 실패 케이스
        @Test
        @DisplayName("잘못된 페이지 번호로 주문 목록 조회 시 InvalidPaginationException 발생")
        void listOrders_WithInvalidPageNumber_ThrowsInvalidPaginationException() {
            // given
            Long memberId = 1L;
            OrderStatus status = OrderStatus.CREATED;
            int invalidPage = -1; // 음수 페이지
            int size = 10;

            // when & then
            assertThatThrownBy(() -> orderService.listOrders(memberId, status, invalidPage, size))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.INVALID_PAGINATION.getCode());
        }

        @Test
        @DisplayName("잘못된 페이지 크기로 주문 목록 조회 시 InvalidPaginationException 발생")
        void listOrders_WithInvalidPageSize_ThrowsInvalidPaginationException() {
            // given
            Long memberId = 1L;
            OrderStatus status = OrderStatus.CREATED;
            int page = 0;
            int invalidSize = 0; // 0 또는 음수 크기

            // when & then
            assertThatThrownBy(() -> orderService.listOrders(memberId, status, page, invalidSize))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.INVALID_PAGINATION.getCode());
        }

        @Test
        @DisplayName("존재하지 않는 회원으로 주문 목록 조회 시 InvalidFilterException 발생")
        void listOrders_WithNonExistentMember_ThrowsInvalidFilterException() {
            // given
            Long nonExistentMemberId = 999L;
            OrderStatus status = OrderStatus.CREATED;
            int page = 0;
            int size = 10;

            given(memberService.findById(nonExistentMemberId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.listOrders(nonExistentMemberId, status, page, size))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.INVALID_FILTER.getCode());
        }

        // 성공 케이스
        @Test
        @DisplayName("유효한 페이지/회원으로 조회 시 빈 목록이라도 정상 반환")
        void listOrders_ReturnsEmptyListWhenNoData() {
            // given
            Long memberId = 1L;
            OrderStatus status = OrderStatus.CREATED;
            int page = 0;
            int size = 10;

            given(memberService.findById(memberId))
                    .willReturn(Optional.of(validMember));
            given(orderRepository.findByMemberIdAndStatus(memberId, status, 
                    PageRequest.of(page, size)))
                    .willReturn(new PageImpl<>(Collections.emptyList()));

            // when
            java.util.List<Order> result = orderService.listOrders(memberId, status, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("회원/상태 미지정 시 기본 페이지 조회도 정상 반환")
        void listOrders_NoFilters_ReturnsEmptyList() {
            // given
            int page = 0;
            int size = 20;

            given(orderRepository.findAll(PageRequest.of(page, size)))
                    .willReturn(new PageImpl<>(Collections.emptyList()));

            // when
            java.util.List<Order> result = orderService.listOrders(null, null, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("상태만 지정해도 정상 반환")
        void listOrders_StatusOnly_ReturnsEmptyList() {
            // given
            OrderStatus status = OrderStatus.CREATED;
            int page = 1;
            int size = 5;

            given(orderRepository.findByStatus(status, 
                    PageRequest.of(page, size)))
                    .willReturn(new PageImpl<>(Collections.emptyList()));

            // when
            java.util.List<Order> result = orderService.listOrders(null, status, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("memberId와 status 모두 지정해도 정상 반환")
        void listOrders_MemberAndStatus_ReturnsEmptyList() {
            // given
            Long memberId = 1L;
            OrderStatus status = OrderStatus.PAID;
            int page = 0;
            int size = 10;

            given(memberService.findById(memberId))
                    .willReturn(Optional.of(validMember));
            given(orderRepository.findByMemberIdAndStatus(memberId, status, 
                    PageRequest.of(page, size)))
                    .willReturn(new PageImpl<>(Collections.emptyList()));

            // when
            java.util.List<Order> result = orderService.listOrders(memberId, status, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("회원 ID와 상태로 실제 주문 목록 조회 성공")
        void listOrders_WithMemberIdAndStatus_ReturnsOrders() {
            // given
            Long memberId = 1L;
            OrderStatus status = OrderStatus.CREATED;
            int page = 0;
            int size = 10;
            
            Order order1 = OrderTestBuilder.builder()
                    .id(1L)
                    .memberId(memberId)
                    .totalAmount(10000L)
                    .status(status)
                    .build();
            Order order2 = OrderTestBuilder.builder()
                    .id(2L)
                    .memberId(memberId)
                    .totalAmount(20000L)
                    .status(status)
                    .build();
            java.util.List<Order> expectedOrders = java.util.List.of(order1, order2);

            given(memberService.findById(memberId))
                    .willReturn(Optional.of(validMember));
            given(orderRepository.findByMemberIdAndStatus(memberId, status, 
                    PageRequest.of(page, size)))
                    .willReturn(new PageImpl<>(expectedOrders));

            // when
            java.util.List<Order> result = orderService.listOrders(memberId, status, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(order1, order2);
        }

        @Test
        @DisplayName("회원 ID만으로 주문 목록 조회 성공")
        void listOrders_WithMemberIdOnly_ReturnsOrders() {
            // given
            Long memberId = 1L;
            int page = 0;
            int size = 10;
            
            Order order1 = OrderTestBuilder.builder()
                    .id(1L)
                    .memberId(memberId)
                    .totalAmount(10000L)
                    .status(OrderStatus.CREATED)
                    .build();
            Order order2 = OrderTestBuilder.builder()
                    .id(2L)
                    .memberId(memberId)
                    .totalAmount(20000L)
                    .status(OrderStatus.PAID)
                    .build();
            java.util.List<Order> expectedOrders = java.util.List.of(order1, order2);

            given(memberService.findById(memberId))
                    .willReturn(Optional.of(validMember));
            given(orderRepository.findByMemberId(memberId, 
                    PageRequest.of(page, size)))
                    .willReturn(new PageImpl<>(expectedOrders));

            // when
            java.util.List<Order> result = orderService.listOrders(memberId, null, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(order1, order2);
        }

        @Test
        @DisplayName("상태만으로 주문 목록 조회 성공")
        void listOrders_WithStatusOnly_ReturnsOrders() {
            // given
            OrderStatus status = OrderStatus.PAID;
            int page = 0;
            int size = 10;
            
            Order order1 = OrderTestBuilder.builder()
                    .id(1L)
                    .memberId(1L)
                    .totalAmount(10000L)
                    .status(status)
                    .build();
            Order order2 = OrderTestBuilder.builder()
                    .id(2L)
                    .memberId(2L)
                    .totalAmount(20000L)
                    .status(status)
                    .build();
            java.util.List<Order> expectedOrders = java.util.List.of(order1, order2);

            given(orderRepository.findByStatus(status, 
                    PageRequest.of(page, size)))
                    .willReturn(new PageImpl<>(expectedOrders));

            // when
            java.util.List<Order> result = orderService.listOrders(null, status, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(order1, order2);
        }

        @Test
        @DisplayName("필터 없이 전체 주문 목록 조회 성공")
        void listOrders_NoFilters_ReturnsAllOrders() {
            // given
            int page = 0;
            int size = 20;
            
            Order order1 = OrderTestBuilder.builder()
                    .id(1L)
                    .memberId(1L)
                    .totalAmount(10000L)
                    .status(OrderStatus.CREATED)
                    .build();
            Order order2 = OrderTestBuilder.builder()
                    .id(2L)
                    .memberId(2L)
                    .totalAmount(20000L)
                    .status(OrderStatus.PAID)
                    .build();
            Order order3 = OrderTestBuilder.builder()
                    .id(3L)
                    .memberId(3L)
                    .totalAmount(30000L)
                    .status(OrderStatus.COMPLETED)
                    .build();
            java.util.List<Order> expectedOrders = java.util.List.of(order1, order2, order3);

            given(orderRepository.findAll(PageRequest.of(page, size)))
                    .willReturn(new PageImpl<>(expectedOrders));

            // when
            java.util.List<Order> result = orderService.listOrders(null, null, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyInAnyOrder(order1, order2, order3);
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrderTest {

        @Test
        @DisplayName("존재하지 않는 주문 취소 시 OrderNotFoundException 발생")
        void cancelOrder_WithNonExistentOrder_ThrowsOrderNotFoundException() {
            // given
            long nonExistentOrderId = 999L;
            String reason = "취소 사유";

            given(orderRepository.findById(nonExistentOrderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(nonExistentOrderId, reason))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.ORDER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("이미 취소된 주문 취소 시 AlreadyCanceledException 발생")
        void cancelOrder_WithAlreadyCanceledOrder_ThrowsAlreadyCanceledException() {
            // given
            long orderId = 1L;
            String reason = "취소 사유";

            Order canceledOrder = OrderTestBuilder.builder()
                    .id(orderId)
                    .memberId(1L)
                    .totalAmount(10000L)
                    .status(OrderStatus.CANCELED)
                    .build();

            given(orderRepository.findById(orderId))
                    .willReturn(Optional.of(canceledOrder));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId, reason))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.ALREADY_CANCELED.getCode());
        }

        @Test
        @DisplayName("이미 결제된 주문 취소 시 InvalidOrderStateException 발생")
        void cancelOrder_WithPaidOrder_ThrowsInvalidOrderStateException() {
            // given
            long orderId = 1L;
            String reason = "취소 사유";

            Order paidOrder = OrderTestBuilder.builder()
                    .id(orderId)
                    .memberId(1L)
                    .totalAmount(10000L)
                    .status(OrderStatus.PAID)
                    .build();

            given(orderRepository.findById(orderId))
                    .willReturn(Optional.of(paidOrder));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId, reason))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.INVALID_ORDER_STATE.getCode());
        }

        @Test
        @DisplayName("중복된 멱등 키로 주문 취소 시 IdempotencyConflictException 발생")
        void cancelOrder_WithDuplicateIdempotencyKey_ThrowsIdempotencyConflictException() {
            // given
            long orderId = 1L;
            String reason = "취소 사유";
            String duplicateKey = "duplicate-cancel-key";

            given(orderRepository.findById(orderId))
                    .willReturn(Optional.of(validOrder));
            
            // TODO: IdempotencyKeyService Mock 설정 (Phase 2 후반에 구현)
            // given(idempotencyKeyService.existsByKeyAndEndpoint(duplicateKey, "POST /api/orders/{orderId}/cancel"))
            //         .willReturn(true);

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(orderId, reason))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.IDEMPOTENCY_CONFLICT.getCode());
        }
        // 성공 케이스
        @Test
        @DisplayName("정상 입력 시 주문 생성되고 OrderCreatedEvent 발행")
        void createOrder_PublishesOrderCreatedEvent() {
            // given
            long memberId = 1L;
            long totalAmount = 10000L;
            String idempotencyKey = "ok-key";

            given(memberService.findById(memberId))
                    .willReturn(Optional.of(validMember));

            // save 시 ID가 설정된 엔티티를 반환하도록 스텁
            Order savedOrder = OrderTestBuilder.builder()
                    .id(10L)
                    .memberId(memberId)
                    .totalAmount(totalAmount)
                    .status(OrderStatus.CREATED)
                    .build();
            given(orderRepository.save(any(Order.class)))
                    .willReturn(savedOrder);

            // 멱등 서비스가 공급자를 실행하도록 설정(명시)
            given(idempotencyKeyService.executeWithIdempotency(eq("POST /api/orders"), eq(idempotencyKey), any(Class.class), any()))
                    .willAnswer(invocation -> {
                        java.util.function.Supplier<Order> op = invocation.getArgument(3);
                        return op.get();
                    });

            // when
            Order result = orderService.createOrder(memberId, totalAmount, idempotencyKey);

            // then: 저장 결과 확인
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(result.getTotalAmount()).isEqualTo(totalAmount);

            // 이벤트 발행 확인
            ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            OrderCreatedEvent captured = eventCaptor.getValue();
            assertThat(captured.getOrderId()).isEqualTo(10L);
            assertThat(captured.getMemberId()).isEqualTo(memberId);
            assertThat(captured.getTotalAmount()).isEqualTo(totalAmount);
        }
    }

    

    @Nested
    @DisplayName("handlePaymentApproved")
    class HandlePaymentApprovedTest {

        @Test
        @DisplayName("존재하지 않는 주문에 대한 결제 승인 시 OrderNotFoundException 발생")
        void handlePaymentApproved_WithNonExistentOrder_ThrowsOrderNotFoundException() {
            // given
            long nonExistentOrderId = 999L;

            given(orderRepository.findById(nonExistentOrderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.handlePaymentApproved(nonExistentOrderId))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.ORDER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("이미 결제된 주문에 대한 중복 승인 시 DuplicateApprovalException 발생")
        void handlePaymentApproved_WithAlreadyPaidOrder_ThrowsDuplicateApprovalException() {
            // given
            long orderId = 1L;

            Order paidOrder = OrderTestBuilder.builder()
                    .id(orderId)
                    .memberId(1L)
                    .totalAmount(10000L)
                    .status(OrderStatus.PAID)
                    .build();

            given(orderRepository.findById(orderId))
                    .willReturn(Optional.of(paidOrder));

            // when & then
            assertThatThrownBy(() -> orderService.handlePaymentApproved(orderId))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.DUPLICATE_APPROVAL.getCode());
        }

        @Test
        @DisplayName("이미 취소된 주문에 대한 결제 승인 시 InvalidOrderStateException 발생")
        void handlePaymentApproved_WithCanceledOrder_ThrowsInvalidOrderStateException() {
            // given
            long orderId = 1L;

            Order canceledOrder = OrderTestBuilder.builder()
                    .id(orderId)
                    .memberId(1L)
                    .totalAmount(10000L)
                    .status(OrderStatus.CANCELED)
                    .build();

            given(orderRepository.findById(orderId))
                    .willReturn(Optional.of(canceledOrder));

            // when & then
            assertThatThrownBy(() -> orderService.handlePaymentApproved(orderId))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.INVALID_ORDER_STATE.getCode());
        }
    }

    @Nested
    @DisplayName("handlePaymentCanceled")
    class HandlePaymentCanceledTest {

        @Test
        @DisplayName("존재하지 않는 주문에 대한 결제 취소 시 OrderNotFoundException 발생")
        void handlePaymentCanceled_WithNonExistentOrder_ThrowsOrderNotFoundException() {
            // given
            long nonExistentOrderId = 999L;

            given(orderRepository.findById(nonExistentOrderId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.handlePaymentCanceled(nonExistentOrderId))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.ORDER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("결제되지 않은 주문에 대한 결제 취소 시 InvalidOrderStateException 발생")
        void handlePaymentCanceled_WithUnpaidOrder_ThrowsInvalidOrderStateException() {
            // given
            long orderId = 1L;

            Order unpaidOrder = OrderTestBuilder.builder()
                    .id(orderId)
                    .memberId(1L)
                    .totalAmount(10000L)
                    .status(OrderStatus.CREATED)
                    .build();

            given(orderRepository.findById(orderId))
                    .willReturn(Optional.of(unpaidOrder));

            // when & then
            assertThatThrownBy(() -> orderService.handlePaymentCanceled(orderId))
                    .isInstanceOf(OrderHandler.class)
                    .hasMessage(OrderErrorStatus.INVALID_ORDER_STATE.getCode());
        }
    }

        @Test
        @DisplayName("결제 승인 처리 시 OrderPaidEvent 발행")
        void handlePaymentApproved_PublishesOrderPaidEvent() {
            // given
            long orderId = 1L;
            
            Order pendingOrder = OrderTestBuilder.builder()
                    .id(orderId)
                    .memberId(1L)
                    .totalAmount(10000L)
                    .status(OrderStatus.PENDING)
                    .build();

            given(orderRepository.findById(orderId))
                    .willReturn(Optional.of(pendingOrder));

            // when
            orderService.handlePaymentApproved(orderId);

            // then
            ArgumentCaptor<OrderPaidEvent> eventCaptor = ArgumentCaptor.forClass(OrderPaidEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            OrderPaidEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
            assertThat(capturedEvent.getPaidAmount()).isEqualTo(pendingOrder.getTotalAmount());
        }
}
