package com.ipia.order.common.exception.order.status;



import org.springframework.http.HttpStatus;

import com.ipia.order.common.exception.general.status.SuccessResponse;

public enum OrderSuccessStatus implements SuccessResponse {

    // 주문 생성
    ORDER_CREATED(HttpStatus.CREATED, "ORDER2001", "주문이 성공적으로 생성되었습니다."),
    
    // 주문 조회
    ORDER_FOUND(HttpStatus.OK, "ORDER2002", "주문 정보를 성공적으로 조회했습니다."),
    ORDERS_FOUND(HttpStatus.OK, "ORDER2003", "주문 목록을 성공적으로 조회했습니다."),
    
    // 주문 상태 변경
    ORDER_PAID(HttpStatus.OK, "ORDER2004", "주문 결제가 완료되었습니다."),
    ORDER_CANCELED(HttpStatus.OK, "ORDER2005", "주문이 성공적으로 취소되었습니다."),
    ORDER_COMPLETED(HttpStatus.OK, "ORDER2006", "주문이 성공적으로 완료되었습니다."),
    
    // 주문 수정
    ORDER_UPDATED(HttpStatus.OK, "ORDER2007", "주문 정보가 성공적으로 수정되었습니다.");



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    OrderSuccessStatus(HttpStatus httpStatus, String code, String message) {
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
