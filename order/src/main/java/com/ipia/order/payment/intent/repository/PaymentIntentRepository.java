package com.ipia.order.payment.intent.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ipia.order.payment.intent.domain.PaymentIntent;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, String> {

    Optional<PaymentIntent> findByIntentId(String intentId);

    @Modifying
    @Query("DELETE FROM PaymentIntent p WHERE p.expiresAt < :now")
    void deleteExpiredIntents(@Param("now") LocalDateTime now);
}


