package com.ipia.order.order.enums;

public enum OrderStatus {
    CREATED,    // 주문 생성됨
    PENDING,    // 결제 진행 중
    PAID,       // 결제 완료
    CANCELED,   // 취소됨
    COMPLETED   // 완료됨
}
