package com.ipia.order.payment.domain;

import com.ipia.order.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 테스트용 Payment 객체 생성을 위한 빌더 클래스
 * 테스트에서만 사용되며 프로덕션 코드에는 영향을 주지 않음
 */
public class PaymentTestBuilder {

    private Long id;
    private Long orderId;
    private BigDecimal paidAmount;
    private BigDecimal canceledAmount = BigDecimal.ZERO;
    private BigDecimal refundedAmount = BigDecimal.ZERO;
    private PaymentStatus status = PaymentStatus.PENDING;
    private String providerTxnId;
    private LocalDateTime approvedAt;
    private LocalDateTime canceledAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    private PaymentTestBuilder() {}

    public static PaymentTestBuilder builder() {
        return new PaymentTestBuilder();
    }

    public PaymentTestBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public PaymentTestBuilder orderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public PaymentTestBuilder paidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
        return this;
    }

    public PaymentTestBuilder canceledAmount(BigDecimal canceledAmount) {
        this.canceledAmount = canceledAmount;
        return this;
    }

    public PaymentTestBuilder refundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
        return this;
    }

    public PaymentTestBuilder status(PaymentStatus status) {
        this.status = status;
        return this;
    }

    public PaymentTestBuilder providerTxnId(String providerTxnId) {
        this.providerTxnId = providerTxnId;
        return this;
    }

    public PaymentTestBuilder approvedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
        return this;
    }

    public PaymentTestBuilder canceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
        return this;
    }

    public PaymentTestBuilder refundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
        return this;
    }

    public PaymentTestBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public PaymentTestBuilder updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public PaymentTestBuilder version(Long version) {
        this.version = version;
        return this;
    }

    public Payment build() {
        // 일반적인 Payment 생성
        Payment payment = Payment.create(orderId, paidAmount, providerTxnId);
        
        // 테스트용 필드 설정이 필요한 경우
        if (id != null || status != PaymentStatus.PENDING || 
            canceledAmount.compareTo(BigDecimal.ZERO) != 0 || 
            refundedAmount.compareTo(BigDecimal.ZERO) != 0 ||
            approvedAt != null || canceledAt != null || refundedAt != null ||
            createdAt != null || updatedAt != null || version != null) {
            
            return createTestPayment(payment);
        }
        
        return payment;
    }

    private Payment createTestPayment(Payment payment) {
        
        try {
            // ID 설정
            java.lang.reflect.Field idField = Payment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(payment, id);

            // 상태 설정
            java.lang.reflect.Field statusField = Payment.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(payment, status);

            // 금액 필드 설정
            java.lang.reflect.Field canceledAmountField = Payment.class.getDeclaredField("canceledAmount");
            canceledAmountField.setAccessible(true);
            canceledAmountField.set(payment, canceledAmount);

            java.lang.reflect.Field refundedAmountField = Payment.class.getDeclaredField("refundedAmount");
            refundedAmountField.setAccessible(true);
            refundedAmountField.set(payment, refundedAmount);

            // 시간 필드 설정
            if (approvedAt != null) {
                java.lang.reflect.Field approvedAtField = Payment.class.getDeclaredField("approvedAt");
                approvedAtField.setAccessible(true);
                approvedAtField.set(payment, approvedAt);
            }

            if (canceledAt != null) {
                java.lang.reflect.Field canceledAtField = Payment.class.getDeclaredField("canceledAt");
                canceledAtField.setAccessible(true);
                canceledAtField.set(payment, canceledAt);
            }

            if (refundedAt != null) {
                java.lang.reflect.Field refundedAtField = Payment.class.getDeclaredField("refundedAt");
                refundedAtField.setAccessible(true);
                refundedAtField.set(payment, refundedAt);
            }

            // BaseEntity 필드 설정
            if (createdAt != null || updatedAt != null) {
                setBaseEntityFields(payment, createdAt, updatedAt);
            }

            // Version 설정
            if (version != null) {
                java.lang.reflect.Field versionField = Payment.class.getDeclaredField("version");
                versionField.setAccessible(true);
                versionField.set(payment, version);
            }

        } catch (Exception e) {
            throw new RuntimeException("테스트용 Payment 생성 실패", e);
        }
        
        return payment;
    }

    private void setBaseEntityFields(Payment payment, LocalDateTime createdAt, LocalDateTime updatedAt) {
        try {
            if (createdAt != null) {
                java.lang.reflect.Field createdAtField = payment.getClass().getSuperclass().getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(payment, createdAt);
            }
            
            if (updatedAt != null) {
                java.lang.reflect.Field updatedAtField = payment.getClass().getSuperclass().getDeclaredField("updatedAt");
                updatedAtField.setAccessible(true);
                updatedAtField.set(payment, updatedAt);
            }
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Payment BaseEntity 필드 설정 실패", e);
        }
    }
}
