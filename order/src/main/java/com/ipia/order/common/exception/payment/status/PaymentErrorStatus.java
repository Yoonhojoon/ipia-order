package com.ipia.order.common.exception.payment.status;

import org.springframework.http.HttpStatus;

import com.ipia.order.common.exception.ExplainError;
import com.ipia.order.common.exception.general.status.ErrorResponse;

public enum PaymentErrorStatus implements ErrorResponse {

    // 결제 기본
    @ExplainError("결제를 찾을 수 없음")
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT4001", "결제를 찾을 수 없습니다."),
    @ExplainError("이미 승인된 결제")
    PAYMENT_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "PAYMENT4002", "이미 승인된 결제입니다."),
    @ExplainError("이미 취소된 결제")
    PAYMENT_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "PAYMENT4003", "이미 취소된 결제입니다."),
    @ExplainError("이미 환불된 결제")
    PAYMENT_ALREADY_REFUNDED(HttpStatus.BAD_REQUEST, "PAYMENT4004", "이미 환불된 결제입니다."),

    // 결제 승인 관련
    @ExplainError("결제 금액이 주문 총액과 일치하지 않음")
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT4005", "결제 금액이 주문 총액과 일치하지 않습니다."),
    @ExplainError("결제 승인 불가능한 상태")
    PAYMENT_CANNOT_APPROVE(HttpStatus.BAD_REQUEST, "PAYMENT4006", "현재 상태에서 결제 승인이 불가능합니다."),
    @ExplainError("중복 결제 승인")
    DUPLICATE_PAYMENT_APPROVAL(HttpStatus.CONFLICT, "PAYMENT4007", "이미 승인된 결제입니다."),

    // 결제 취소 관련
    @ExplainError("결제 취소 불가능한 상태")
    PAYMENT_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "PAYMENT4008", "현재 상태에서 결제 취소가 불가능합니다."),
    @ExplainError("취소 금액이 결제 금액을 초과")
    CANCEL_AMOUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "PAYMENT4009", "취소 금액이 결제 금액을 초과할 수 없습니다."),
    @ExplainError("유효하지 않은 취소 금액")
    INVALID_CANCEL_AMOUNT(HttpStatus.BAD_REQUEST, "PAYMENT4010", "취소 금액은 0보다 커야 합니다."),
    @ExplainError("결제되지 않은 주문")
    UNPAID_ORDER(HttpStatus.BAD_REQUEST, "PAYMENT4011", "결제되지 않은 주문입니다."),

    // 결제 환불 관련
    @ExplainError("결제 환불 불가능한 상태")
    PAYMENT_CANNOT_REFUND(HttpStatus.BAD_REQUEST, "PAYMENT4012", "현재 상태에서 결제 환불이 불가능합니다."),
    @ExplainError("환불 금액이 취소 금액을 초과")
    REFUND_AMOUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "PAYMENT4013", "환불 금액이 취소 금액을 초과할 수 없습니다."),
    @ExplainError("유효하지 않은 환불 금액")
    INVALID_REFUND_AMOUNT(HttpStatus.BAD_REQUEST, "PAYMENT4014", "환불 금액은 0보다 커야 합니다."),

    // 입력값 검증 관련
    @ExplainError("주문 ID가 필수 아님")
    ORDER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "PAYMENT4015", "주문 ID는 필수입니다."),
    @ExplainError("결제 금액이 필수 아님")
    PAYMENT_AMOUNT_REQUIRED(HttpStatus.BAD_REQUEST, "PAYMENT4016", "결제 금액은 필수입니다."),
    @ExplainError("외부 거래 ID가 필수 아님")
    PROVIDER_TXN_ID_REQUIRED(HttpStatus.BAD_REQUEST, "PAYMENT4017", "외부 거래 ID는 필수입니다."),
    @ExplainError("유효하지 않은 결제 상태")
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "PAYMENT4018", "유효하지 않은 결제 상태입니다."),

    // Toss API 관련
    @ExplainError("Toss API 오류")
    TOSS_API_ERROR(HttpStatus.BAD_GATEWAY, "PAYMENT4019", "Toss 결제 API 오류가 발생했습니다."),
    @ExplainError("Toss 네트워크 오류")
    TOSS_NETWORK_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT4020", "Toss API 네트워크 오류가 발생했습니다."),
    @ExplainError("Toss API 응답 파싱 오류")
    TOSS_RESPONSE_PARSE_ERROR(HttpStatus.BAD_GATEWAY, "PAYMENT4021", "Toss API 응답을 파싱할 수 없습니다."),
    @ExplainError("Toss 웹훅 서명 검증 실패")
    TOSS_WEBHOOK_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "PAYMENT4022", "Toss 웹훅 서명이 유효하지 않습니다."),
    @ExplainError("중복 웹훅 처리")
    DUPLICATE_WEBHOOK(HttpStatus.CONFLICT, "PAYMENT4023", "이미 처리된 웹훅입니다."),

    // 권한 관련
    @ExplainError("권한 없는 결제 조회")
    PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PAYMENT4024", "해당 결제에 대한 접근 권한이 없습니다."),
    @ExplainError("권한 없는 주문 결제 조회")
    ORDER_PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PAYMENT4025", "해당 주문의 결제 정보에 대한 접근 권한이 없습니다."),

    // 멱등성 관련
    @ExplainError("결제 멱등 키 충돌")
    PAYMENT_IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "PAYMENT4026", "동일한 멱등 키로 이미 처리된 결제 요청입니다."),
    @ExplainError("유효하지 않은 멱등 키")
    INVALID_IDEMPOTENCY_KEY(HttpStatus.BAD_REQUEST, "PAYMENT4027", "유효하지 않은 멱등 키입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    PaymentErrorStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getErrorStatus() { return httpStatus; }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
