package com.ipia.order.common.exception.idempotency.status;



import org.springframework.http.HttpStatus;

import com.ipia.order.common.exception.ExplainError;
import com.ipia.order.common.exception.general.status.ErrorResponse;

public enum IdempotencyErrorStatus implements ErrorResponse {

    @ExplainError("유효하지 않은 멱등 키")
    INVALID_IDEMPOTENCY_KEY(HttpStatus.BAD_REQUEST, "IDEMP4001", "멱등 키가 유효하지 않습니다."),

    @ExplainError("동일 키로 동시 처리 충돌")
    CONCURRENT_CONFLICT(HttpStatus.CONFLICT, "IDEMP4002", "동일 멱등 키로 이미 처리가 진행 중입니다."),

    @ExplainError("이미 처리된 키")
    DUPLICATE_KEY(HttpStatus.CONFLICT, "IDEMP4003", "이미 처리된 멱등 키입니다."),

    @ExplainError("응답 직렬화 실패")
    RESPONSE_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "IDEMP5001", "응답 직렬화에 실패했습니다."),

    @ExplainError("저장소 처리 오류")
    REPOSITORY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "IDEMP5002", "멱등 키 저장/조회 중 오류가 발생했습니다.");



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    IdempotencyErrorStatus(HttpStatus httpStatus, String code, String message) {
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


