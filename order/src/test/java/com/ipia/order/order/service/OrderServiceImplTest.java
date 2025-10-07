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

import com.ipia.order.member.domain.Member;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.order.domain.Order;
import com.ipia.order.order.enums.OrderStatus;
import com.ipia.order.order.event.OrderCanceledEvent;
import com.ipia.order.order.event.OrderCreatedEvent;
import com.ipia.order.order.event.OrderPaidEvent;
import com.ipia.order.order.repository.OrderRepository;
import com.ipia.order.common.exception.order.OrderHandler;
import com.ipia.order.common.exception.order.status.OrderErrorStatus;

import java.util.Optional;

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
    
    // TODO: IdempotencyKeyService Mock 추가 (Phase 2 후반에 구현 예정)
    
    @InjectMocks
    private OrderServiceImpl orderService;

    private Member validMember;
    private Order validOrder;

    @BeforeEach
    void setUp() {
        validMember = Member.createTestMember(1L, "홍길동", "hong@example.com", "encodedPassword123!", null);

        validOrder = Order.createTestOrder(1L, 1L, 10000L, OrderStatus.CREATED);
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
                java.lang.reflect.Field isActiveField = Member.class.getDeclaredField("isActive");
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
            
            // TODO: IdempotencyKeyService Mock 설정 (Phase 2 후반에 구현)
            // given(idempotencyKeyService.existsByKeyAndEndpoint(duplicateKey, "POST /api/orders"))
            //         .willReturn(true);

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

            Order order = Order.createTestOrder(orderId, 1L, 10000L, OrderStatus.CREATED); // 다른 회원의 주문

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

            // when
            java.util.List<Order> result = orderService.listOrders(memberId, status, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
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

            Order canceledOrder = Order.createTestOrder(orderId, 1L, 10000L, OrderStatus.CANCELED);

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

            Order paidOrder = Order.createTestOrder(orderId, 1L, 10000L, OrderStatus.PAID);

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
            Order savedOrder = Order.createTestOrder(10L, memberId, totalAmount, OrderStatus.CREATED);
            given(orderRepository.save(any(Order.class)))
                    .willReturn(savedOrder);

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

            Order paidOrder = Order.createTestOrder(orderId, 1L, 10000L, OrderStatus.PAID);

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

            Order canceledOrder = Order.createTestOrder(orderId, 1L, 10000L, OrderStatus.CANCELED);

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

            Order unpaidOrder = Order.createTestOrder(orderId, 1L, 10000L, OrderStatus.CREATED);

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
            
            Order pendingOrder = Order.createTestOrder(orderId, 1L, 10000L, OrderStatus.PENDING);

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
