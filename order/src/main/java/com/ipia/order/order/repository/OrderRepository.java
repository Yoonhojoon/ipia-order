package com.ipia.order.order.repository;

import com.ipia.order.order.domain.Order;
import com.ipia.order.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 주문 리포지토리
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 회원 ID와 상태로 주문 조회 (페이지네이션)
     * 
     * @param memberId 회원 ID
     * @param status 주문 상태
     * @param pageable 페이지네이션 정보
     * @return 주문 목록
     */
    Page<Order> findByMemberIdAndStatus(Long memberId, OrderStatus status, Pageable pageable);
    
    /**
     * 회원 ID로 주문 조회 (페이지네이션)
     * 
     * @param memberId 회원 ID
     * @param pageable 페이지네이션 정보
     * @return 주문 목록
     */
    Page<Order> findByMemberId(Long memberId, Pageable pageable);
    
    /**
     * 상태로 주문 조회 (페이지네이션)
     * 
     * @param status 주문 상태
     * @param pageable 페이지네이션 정보
     * @return 주문 목록
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * 회원 ID로 주문 목록 조회
     * 
     * @param memberId 회원 ID
     * @return 주문 목록
     */
    List<Order> findByMemberId(Long memberId);
    
    /**
     * 상태로 주문 목록 조회
     * 
     * @param status 주문 상태
     * @return 주문 목록
     */
    List<Order> findByStatus(OrderStatus status);
}
