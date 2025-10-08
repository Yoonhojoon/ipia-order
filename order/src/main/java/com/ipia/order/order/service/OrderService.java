package com.ipia.order.order.service;

import com.ipia.order.order.domain.Order;
import com.ipia.order.order.enums.OrderStatus;
import com.ipia.order.web.dto.response.order.OrderResponse;
import com.ipia.order.web.dto.response.order.OrderListResponse;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 주문 서비스 인터페이스
 * 
 * 주요 기능:
 * - 주문 생성 (멱등성 지원)
 * - 주문 조회 (단건/목록)
 * - 주문 취소 (비즈니스 취소)
 * - 결제 이벤트 핸들링
 */
public interface OrderService {
    
    /**
     * 주문 생성
     * 
     * @param memberId 회원 ID
     * @param totalAmount 주문 총액 (0보다 커야 함)
     * @param idempotencyKey 멱등성 키 (선택사항)
     * @return 생성된 주문
     * @throws MemberNotFoundException 존재하지 않는 회원
     * @throws InvalidAmountException 음수 또는 0원 주문 금액
     * @throws InactiveMemberException 비활성 회원
     * @throws IdempotencyConflictException 멱등 키 중복
     */
    Order createOrder(long memberId, long totalAmount, @Nullable String idempotencyKey);
    
    /**
     * 주문 단건 조회
     * 
     * @param orderId 주문 ID
     * @return 주문 정보 (없으면 Optional.empty())
     * @throws OrderNotFoundException 존재하지 않는 주문
     * @throws AccessDeniedException 권한 없는 주문 조회
     */
    Optional<Order> getOrder(long orderId);
    
    /**
     * 주문 목록 조회 (페이지네이션)
     * 
     * @param memberId 회원 ID (선택사항)
     * @param status 주문 상태 문자열 (선택사항)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 주문 목록 응답 DTO
     * @throws InvalidFilterException 잘못된 필터 조건
     * @throws InvalidPaginationException 잘못된 페이지네이션 파라미터
     */
    OrderListResponse listOrders(@Nullable Long memberId, @Nullable String status, int page, int size);
    
    /**
     * 주문 취소 (비즈니스 취소)
     * 
     * @param orderId 주문 ID
     * @param reason 취소 사유 (선택사항)
     * @return 취소된 주문
     * @throws OrderNotFoundException 존재하지 않는 주문
     * @throws InvalidOrderStateException 잘못된 상태에서 취소 시도
     * @throws AlreadyCanceledException 이미 취소된 주문
     * @throws IdempotencyConflictException 멱등 키 중복
     */
    Order cancelOrder(long orderId, @Nullable String reason);
    
    /**
     * 결제 승인 이벤트 핸들링
     * 
     * @param orderId 주문 ID
     * @throws OrderNotFoundException 존재하지 않는 주문
     * @throws InvalidOrderStateException 잘못된 상태에서 승인 시도
     * @throws DuplicateApprovalException 중복 승인 시도
     */
    void handlePaymentApproved(long orderId);
    
    /**
     * 결제 취소 이벤트 핸들링
     * 
     * @param orderId 주문 ID
     * @throws OrderNotFoundException 존재하지 않는 주문
     * @throws InvalidOrderStateException 잘못된 상태에서 취소 시도
     */
    void handlePaymentCanceled(long orderId);
}
