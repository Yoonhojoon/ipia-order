package com.ipia.order.common.exception.auth.status;

import org.springframework.http.HttpStatus;

import com.ipia.order.common.exception.general.status.SuccessResponse;

public enum AuthSuccessStatus implements SuccessResponse {

    // ==== 인증 관련 성공 ====
    LOGIN_SUCCESS(HttpStatus.OK, "AUTH2001", "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH2002", "로그아웃에 성공했습니다."),
    TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "AUTH2003", "토큰 갱신에 성공했습니다."),
    REGISTER_SUCCESS(HttpStatus.OK, "AUTH2004", "회원가입에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    AuthSuccessStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getSuccessStatus() { 
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
