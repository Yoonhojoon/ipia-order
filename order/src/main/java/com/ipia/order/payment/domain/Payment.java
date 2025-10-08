package com.ipia.order.payment.domain;

import com.ipia.order.common.exception.payment.PaymentHandler;
import com.ipia.order.common.exception.payment.status.PaymentErrorStatus;
import com.ipia.order.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 정보를 나타내는 엔티티
 * 
 * 주문별 결제 승인/취소/환불 정보를 관리하며,
 * Toss Payments API와 연동하여 외부 결제 시스템과 통신
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주문 ID (외래키)
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 결제 금액
     */
    @Column(name = "paid_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal paidAmount;

    /**
     * 취소 금액
     */
    @Column(name = "canceled_amount", precision = 19, scale = 2)
    private BigDecimal canceledAmount = BigDecimal.ZERO;

    /**
     * 환불 금액
     */
    @Column(name = "refunded_amount", precision = 19, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    /**
     * 결제 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * 외부 결제 시스템 거래 ID (Toss Payment Key 등)
     */
    @Column(name = "provider_txn_id")
    private String providerTxnId;

    /**
     * 결제 승인 일시
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 결제 취소 일시
     */
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    /**
     * 결제 환불 일시
     */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    /**
     * 생성 일시
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 수정 일시
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 낙관적 락을 위한 버전
     */
    @Version
    private Long version;

    /**
     * 결제 생성 (팩토리 메서드)
     * 
     * @param orderId 주문 ID
     * @param paidAmount 결제 금액
     * @param providerTxnId 외부 결제 시스템 거래 ID
     * @return 생성된 Payment 엔티티
     */
    public static Payment create(Long orderId, BigDecimal paidAmount, String providerTxnId) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.paidAmount = paidAmount;
        payment.providerTxnId = providerTxnId;
        payment.status = PaymentStatus.PENDING;
        return payment;
    }

    /**
     * 결제 승인 처리
     * 
     * @param orderTotalAmount 주문 총액 (검증용)
     * @throws PaymentHandler 현재 상태에서 승인 불가능한 경우 또는 결제 금액 불일치
     */
    public void approve(BigDecimal orderTotalAmount) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 결제 취소 처리
     * 
     * @param cancelAmount 취소 금액
     * @param reason 취소 사유
     * @throws PaymentHandler 현재 상태에서 취소 불가능한 경우 또는 취소 금액 초과
     */
    public void cancel(BigDecimal cancelAmount, String reason) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 결제 환불 처리
     * 
     * @param refundAmount 환불 금액
     * @param reason 환불 사유
     * @throws PaymentHandler 현재 상태에서 환불 불가능한 경우 또는 환불 금액 초과
     */
    public void refund(BigDecimal refundAmount, String reason) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 결제가 완료되었는지 확인 (승인된 상태)
     */
    public boolean isApproved() {
        return status == PaymentStatus.APPROVED;
    }

    /**
     * 결제가 취소되었는지 확인
     */
    public boolean isCanceled() {
        return status == PaymentStatus.CANCELED;
    }

    /**
     * 결제가 환불되었는지 확인
     */
    public boolean isRefunded() {
        return status == PaymentStatus.REFUNDED;
    }

    /**
     * 결제가 대기 상태인지 확인
     */
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
}
