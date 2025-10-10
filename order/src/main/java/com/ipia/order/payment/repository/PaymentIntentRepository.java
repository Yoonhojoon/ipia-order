package com.ipia.order.payment.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ipia.order.payment.domain.PaymentIntent;

/**
 * PaymentIntent 도메인 저장소
 */
@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, String> {

    /**
     * Intent ID로 조회
     * 
     * @param intentId 의도 ID
     * @return PaymentIntent 엔티티 또는 empty
     */
    Optional<PaymentIntent> findByIntentId(String intentId);

    /**
     * 만료된 PaymentIntent 삭제
     * 
     * @param now 현재 시간
     */
    @Modifying
    @Query("DELETE FROM PaymentIntent p WHERE p.expiresAt < :now")
    void deleteExpiredIntents(@Param("now") LocalDateTime now);
}
