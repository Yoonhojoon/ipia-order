package com.ipia.order.order;

import com.ipia.order.common.util.JwtUtil;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.enums.MemberRole;
import com.ipia.order.member.repository.MemberRepository;
import com.ipia.order.order.domain.Order;
import com.ipia.order.order.enums.OrderStatus;
import com.ipia.order.order.repository.OrderRepository;
import com.ipia.order.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderDomainIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("주문 생성 시 실제 저장 및 기본 상태 확인")
    void createOrder_persistAndDefaultState() {
        // given
        Member member = memberRepository.save(Member.builder()
                .name("tester")
                .email("tester@example.com")
                .password("encoded")
                .role(MemberRole.USER)
                .build());
        long memberId = member.getId();
        long amount = 10_000L;

        // when
        Order created = orderService.createOrder(memberId, amount, null);

        // then
        Order found = orderRepository.findById(created.getId()).orElseThrow();
        assertThat(found.getMemberId()).isEqualTo(memberId);
        assertThat(found.getTotalAmount()).isEqualTo(amount);
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("주문 목록 조회: memberId/status 조합 및 페이지네이션 검증")
    void listOrders_filterAndPagination() {
        // given
        Member m1 = memberRepository.save(Member.builder()
                .name("mem1")
                .email("mem1@example.com")
                .password("encoded")
                .role(MemberRole.USER)
                .build());
        Member m2 = memberRepository.save(Member.builder()
                .name("mem2")
                .email("mem2@example.com")
                .password("encoded")
                .role(MemberRole.USER)
                .build());

        // m1: CREATED 3건, CONFIRMED 2건, COMPLETED 1건 생성
        long[] amounts = {1000,2000,3000,4000,5000,6000};
        for (int i = 0; i < amounts.length; i++) {
            Order o = orderService.createOrder(m1.getId(), amounts[i], null);
            Order persisted = orderRepository.findById(o.getId()).orElseThrow();
            if (i >= 3 && i < 5) {
                persisted.confirm();
            } else if (i == 5) {
                persisted.confirm();
                persisted.startFulfillment();
                persisted.ship();
                persisted.deliver();
                persisted.complete();
            }
            orderRepository.save(persisted);
        }

        // m2: CREATED 2건
        orderService.createOrder(m2.getId(), 7000, null);
        orderService.createOrder(m2.getId(), 8000, null);

        // when/then
        // 1) memberId+m1 & status=CREATED, size=2 페이지 0
        var res1 = orderService.listOrders(m1.getId(), "CREATED", 0, 2);
        assertThat(res1.getOrders()).hasSize(2);
        assertThat(res1.getTotalCount()).isGreaterThanOrEqualTo(3);

        // 2) memberId=m1만, size=3 페이지 1
        var res2 = orderService.listOrders(m1.getId(), null, 1, 3);
        assertThat(res2.getPage()).isEqualTo(1);
        assertThat(res2.getOrders().size()).isBetween(0, 3);

        // 3) status=COMPLETED만, size=10 페이지 0
        var res3 = orderService.listOrders(null, "COMPLETED", 0, 10);
        assertThat(res3.getOrders()).extracting("status").containsOnly(OrderStatus.COMPLETED);

        // 4) 필터 없음, size=5 페이지 0
        var res4 = orderService.listOrders(null, null, 0, 5);
        assertThat(res4.getOrders().size()).isBetween(1, 5);
    }

    @Test
    @DisplayName("승인 처리 시 상태가 CONFIRMED로 전이된다")
    void handlePaymentApproved_transitionToConfirmed() {
        // given
        Member member = memberRepository.save(Member.builder()
                .name("payer")
                .email("payer@example.com")
                .password("encoded")
                .role(MemberRole.USER)
                .build());
        Order created = orderService.createOrder(member.getId(), 20_000L, null);

        // 승인 이전 상태 유지(CREATED)

        // when
        orderService.handlePaymentApproved(created.getId());

        // then
        Order found = orderRepository.findById(created.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("주문 취소 처리 시 상태가 CANCELED로 전이된다")
    void cancelOrder_transitionToCanceled() {
        // given
        Member member = memberRepository.save(Member.builder()
                .name("canceler")
                .email("canceler@example.com")
                .password("encoded")
                .role(MemberRole.USER)
                .build());
        Order created = orderService.createOrder(member.getId(), 30_000L, null);

        // when: CREATED 단계에서 취소 요청 후 취소
        Order pendingCancel = orderRepository.findById(created.getId()).orElseThrow();
        pendingCancel.requestCancel();
        orderRepository.save(pendingCancel);

        Order canceled = orderService.cancelOrder(created.getId(), null);

        // then
        assertThat(canceled.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("멱등성 키 재요청 시 동일 주문이 반환되고 중복 생성이 발생하지 않는다")
    void createOrder_idempotentReplaysReturnSameOrder() {
        // given
        Member member = memberRepository.save(Member.builder()
                .name("idem")
                .email("idem@example.com")
                .password("encoded")
                .role(MemberRole.USER)
                .build());
        String idemKey = "test-idem-123";

        // when
        Order first = orderService.createOrder(member.getId(), 12_345L, idemKey);
        Order second = orderService.createOrder(member.getId(), 12_345L, idemKey);

        // then
        assertThat(second.getId()).isEqualTo(first.getId());
        var allByMember = orderRepository.findByMemberId(member.getId());
        long count = allByMember.stream().filter(o -> o.getTotalAmount() == 12_345L).count();
        assertThat(count).isEqualTo(1);
    }
}


