package com.ipia.order.order.enums;

public enum OrderStatus {
    // 기본 상태
    CREATED,        // 주문 생성됨
    CANCELED,       // 취소됨
    COMPLETED,      // 완료됨

    // 이행 중심 상태
    PLACED,                 // 주문 접수
    CONFIRMED,              // 승인/확인 (결제 승인 시 매핑)
    FULFILLMENT_STARTED,    // 이행 시작
    SHIPPED,                // 출고/배송 시작
    DELIVERED,              // 고객 인도
    CANCEL_REQUESTED        // 취소 요청 상태 (중간 표시용)
}
