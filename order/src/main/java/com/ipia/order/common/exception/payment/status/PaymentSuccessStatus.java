package com.ipia.order.common.exception.payment.status;

import org.springframework.http.HttpStatus;

import com.ipia.order.common.exception.general.status.SuccessResponse;

public enum PaymentSuccessStatus implements SuccessResponse {

    // 결제 승인
    PAYMENT_APPROVED(HttpStatus.OK, "PAYMENT2001", "결제가 성공적으로 승인되었습니다."),
    
    // 결제 취소
    PAYMENT_CANCELED(HttpStatus.OK, "PAYMENT2002", "결제가 성공적으로 취소되었습니다."),
    
    // 결제 환불
    PAYMENT_REFUNDED(HttpStatus.OK, "PAYMENT2003", "결제가 성공적으로 환불되었습니다."),
    
    // 결제 조회
    PAYMENT_FOUND(HttpStatus.OK, "PAYMENT2004", "결제 정보를 성공적으로 조회했습니다."),
    PAYMENTS_FOUND(HttpStatus.OK, "PAYMENT2005", "결제 목록을 성공적으로 조회했습니다."),
    
    // 주문별 결제 조회
    ORDER_PAYMENTS_FOUND(HttpStatus.OK, "PAYMENT2006", "주문의 결제 정보를 성공적으로 조회했습니다."),
    
    // 웹훅 처리
    WEBHOOK_PROCESSED(HttpStatus.OK, "PAYMENT2007", "웹훅이 성공적으로 처리되었습니다."),
    
    // 결제 상태 동기화
    PAYMENT_STATUS_SYNCED(HttpStatus.OK, "PAYMENT2008", "결제 상태가 성공적으로 동기화되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    PaymentSuccessStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getSuccessStatus() { return httpStatus; }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
