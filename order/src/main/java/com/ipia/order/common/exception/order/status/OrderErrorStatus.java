package com.ipia.order.common.exception.order.status;



import org.springframework.http.HttpStatus;

import com.ipia.order.common.exception.ExplainError;
import com.ipia.order.common.exception.general.status.ErrorResponse;

public enum OrderErrorStatus implements ErrorResponse {

    // 주문
    @ExplainError("주문을 찾을 수 없음")
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER4001", "주문을 찾을 수 없습니다."),
    @ExplainError("이미 취소된 주문")
    ORDER_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "ORDER4002", "이미 취소된 주문입니다."),
    @ExplainError("이미 완료된 주문")
    ORDER_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "ORDER4003", "이미 완료된 주문입니다."),
    @ExplainError("이미 결제된 주문")
    ORDER_ALREADY_PAID(HttpStatus.BAD_REQUEST, "ORDER4004", "이미 결제된 주문입니다."),

    // 주문 생성 관련
    @ExplainError("유효하지 않은 주문 금액")
    INVALID_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "ORDER4005", "주문 금액은 0보다 커야 합니다."),
    @ExplainError("주문 금액이 음수")
    NEGATIVE_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "ORDER4006", "주문 금액은 음수일 수 없습니다."),
    @ExplainError("회원 ID가 필수 아님")
    MEMBER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORDER4007", "회원 ID는 필수입니다."),

    // 주문 상태 전이 관련
    @ExplainError("잘못된 주문 상태 전이 - 결제 완료")
    INVALID_TRANSITION_TO_PAID(HttpStatus.BAD_REQUEST, "ORDER4008", "현재 상태에서 결제 완료로 전이할 수 없습니다."),
    @ExplainError("잘못된 주문 상태 전이 - 취소")
    INVALID_TRANSITION_TO_CANCELED(HttpStatus.BAD_REQUEST, "ORDER4009", "현재 상태에서 취소로 전이할 수 없습니다."),
    @ExplainError("잘못된 주문 상태 전이 - 완료")
    INVALID_TRANSITION_TO_COMPLETED(HttpStatus.BAD_REQUEST, "ORDER4010", "현재 상태에서 완료로 전이할 수 없습니다."),
    @ExplainError("잘못된 주문 상태 전이 - 결제 진행 중")
    INVALID_TRANSITION_TO_PENDING(HttpStatus.BAD_REQUEST, "ORDER4011", "현재 상태에서 결제 진행 중으로 전이할 수 없습니다."),

    // 입력값 검증 관련
    @ExplainError("주문 ID가 필수 아님")
    ORDER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORDER4012", "주문 ID는 필수입니다."),
    @ExplainError("주문 상태가 필수 아님")
    ORDER_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "ORDER4013", "주문 상태는 필수입니다."),
    @ExplainError("유효하지 않은 주문 상태")
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "ORDER4014", "유효하지 않은 주문 상태입니다."),

    // OrderService 관련 추가 예외들
    @ExplainError("존재하지 않는 회원")
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER4015", "존재하지 않는 회원입니다."),
    @ExplainError("비활성 회원")
    INACTIVE_MEMBER(HttpStatus.BAD_REQUEST, "ORDER4016", "비활성 회원입니다."),
    @ExplainError("유효하지 않은 주문 금액")
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "ORDER4017", "주문 금액은 0보다 커야 합니다."),
    @ExplainError("멱등 키 충돌")
    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "ORDER4018", "동일한 멱등 키로 이미 처리된 요청입니다."),
    @ExplainError("권한 없는 주문 조회")
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORDER4019", "해당 주문에 대한 접근 권한이 없습니다."),
    @ExplainError("잘못된 필터 조건")
    INVALID_FILTER(HttpStatus.BAD_REQUEST, "ORDER4020", "잘못된 필터 조건입니다."),
    @ExplainError("잘못된 페이지네이션 파라미터")
    INVALID_PAGINATION(HttpStatus.BAD_REQUEST, "ORDER4021", "잘못된 페이지네이션 파라미터입니다."),
    @ExplainError("잘못된 주문 상태")
    INVALID_ORDER_STATE(HttpStatus.BAD_REQUEST, "ORDER4022", "잘못된 주문 상태입니다."),
    @ExplainError("이미 취소된 주문")
    ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "ORDER4023", "이미 취소된 주문입니다."),
    @ExplainError("중복 승인 시도")
    DUPLICATE_APPROVAL(HttpStatus.CONFLICT, "ORDER4024", "이미 승인된 주문입니다.");




    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    OrderErrorStatus(HttpStatus httpStatus, String code, String message) {
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
