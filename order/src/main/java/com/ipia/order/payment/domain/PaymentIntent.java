package com.ipia.order.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ipia.order.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 의도 정보를 나타내는 엔티티
 * 
 * 결제 승인 전 임시 저장되는 결제 의도 정보를 관리하며,
 * Redis 실패 시 DB에 fallback 저장
 */
@Entity
@Table(name = "payment_intents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentIntent extends BaseEntity {

    @Id
    @Column(name = "intent_id", nullable = false, unique = true)
    private String intentId;

    /**
     * 주문 ID (외래키)
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 결제 금액
     */
    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * 성공 URL
     */
    @Column(name = "success_url", nullable = false)
    private String successUrl;

    /**
     * 실패 URL
     */
    @Column(name = "fail_url", nullable = false)
    private String failUrl;

    /**
     * 멱등성 키
     */
    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    /**
     * 만료 일시
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder(access = AccessLevel.PROTECTED)
    private PaymentIntent(String intentId, Long orderId, BigDecimal amount, 
                         String successUrl, String failUrl, String idempotencyKey, 
                         LocalDateTime expiresAt) {
        validateIntentId(intentId);
        validateOrderId(orderId);
        validateAmount(amount);
        validateUrls(successUrl, failUrl);
        validateIdempotencyKey(idempotencyKey);
        validateExpiresAt(expiresAt);
        
        this.intentId = intentId;
        this.orderId = orderId;
        this.amount = amount;
        this.successUrl = successUrl;
        this.failUrl = failUrl;
        this.idempotencyKey = idempotencyKey;
        this.expiresAt = expiresAt;
    }

    /**
     * 결제 의도 생성 (팩토리 메서드)
     * 
     * @param intentId 의도 ID
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @param successUrl 성공 URL
     * @param failUrl 실패 URL
     * @param idempotencyKey 멱등성 키
     * @param ttlSeconds TTL (초)
     * @return 생성된 PaymentIntent 엔티티
     */
    public static PaymentIntent create(String intentId, Long orderId, BigDecimal amount, 
                                     String successUrl, String failUrl, String idempotencyKey, 
                                     long ttlSeconds) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(ttlSeconds);
        return PaymentIntent.builder()
                .intentId(intentId)
                .orderId(orderId)
                .amount(amount)
                .successUrl(successUrl)
                .failUrl(failUrl)
                .idempotencyKey(idempotencyKey)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // ==================== 검증 메서드 ====================

    private void validateIntentId(String intentId) {
        if (intentId == null || intentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Intent ID는 필수입니다.");
        }
    }

    private void validateOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID는 양수여야 합니다.");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("결제 금액은 양수여야 합니다.");
        }
    }

    private void validateUrls(String successUrl, String failUrl) {
        if (successUrl == null || successUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Success URL은 필수입니다.");
        }
        if (failUrl == null || failUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Fail URL은 필수입니다.");
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency Key는 필수입니다.");
        }
    }

    private void validateExpiresAt(LocalDateTime expiresAt) {
        if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료 일시는 현재 시간 이후여야 합니다.");
        }
    }
}
