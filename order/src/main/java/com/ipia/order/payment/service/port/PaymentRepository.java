package com.ipia.order.payment.service.port;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipia.order.payment.domain.Payment;

/**
 * Payment 도메인 저장소 포트
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 주문 ID로 Payment 조회
     * 
     * @param orderId 주문 ID
     * @return Payment 엔티티 또는 empty
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * 외부 거래 ID로 Payment 조회
     * 
     * @param providerTxnId 외부 거래 ID (Toss Payment Key)
     * @return Payment 엔티티 또는 empty
     */
    Optional<Payment> findByProviderTxnId(String providerTxnId);
}
