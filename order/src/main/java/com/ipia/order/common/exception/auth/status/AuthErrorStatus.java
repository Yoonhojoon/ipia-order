package com.ipia.order.common.exception.auth.status;

import org.springframework.http.HttpStatus;

import com.ipia.order.common.exception.general.status.ErrorResponse;

public enum AuthErrorStatus implements ErrorResponse {

    // ==== 인증 관련 오류 ====
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4001", "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4002", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4003", "토큰이 만료되었습니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH4004", "토큰이 존재하지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH4005", "회원 정보를 찾을 수 없습니다."),
    INACTIVE_MEMBER(HttpStatus.UNAUTHORIZED, "AUTH4006", "비활성화된 회원입니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH4007", "이미 존재하는 회원입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    AuthErrorStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getErrorStatus() { 
        return httpStatus; 
    }

    @Override
    public String getCode() { 
        return code; 
    }

    @Override
    public String getMessage() { 
        return message; 
    }
}
