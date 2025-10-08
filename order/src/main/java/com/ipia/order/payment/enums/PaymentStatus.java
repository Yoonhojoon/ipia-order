package com.ipia.order.payment.enums;

/**
 * 결제 상태를 나타내는 enum
 * 
 * 상태 전이: PENDING → APPROVED → CANCELED → REFUNDED
 */
public enum PaymentStatus {
    
    /**
     * 결제 대기 상태 (주문 생성 후 결제 승인 전)
     */
    PENDING("결제 대기"),
    
    /**
     * 결제 승인 완료 상태
     */
    APPROVED("결제 승인"),
    
    /**
     * 결제 취소 상태
     */
    CANCELED("결제 취소"),
    
    /**
     * 결제 환불 상태
     */
    REFUNDED("결제 환불");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 결제 승인이 가능한 상태인지 확인
     * PENDING 상태에서만 승인 가능
     */
    public boolean canApprove() {
        return this == PENDING;
    }
    
    /**
     * 결제 취소가 가능한 상태인지 확인
     * APPROVED 상태에서만 취소 가능
     */
    public boolean canCancel() {
        return this == APPROVED;
    }
    
    /**
     * 결제 환불이 가능한 상태인지 확인
     * CANCELED 상태에서만 환불 가능
     */
    public boolean canRefund() {
        return this == CANCELED;
    }
}
