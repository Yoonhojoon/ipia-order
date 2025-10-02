package com.ipia.order.common.exception.member.status;



import com.ipia.order.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum MemberSuccessStatus implements SuccessResponse {

    // 로그인/회원가입
    SIGN_IN_SUCCESS(HttpStatus.OK, "MEMBER2000", "로그인이 완료되었습니다."),
    SIGN_UP_SUCCESS(HttpStatus.CREATED, "MEMBER2001", "회원가입이 완료되었습니다."),
    SIGN_OUT_SUCCESS(HttpStatus.OK, "MEMBER2002", "로그아웃이 완료되었습니다.");



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    MemberSuccessStatus(HttpStatus httpStatus, String code, String message) {
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
