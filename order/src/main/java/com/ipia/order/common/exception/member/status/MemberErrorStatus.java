package com.ipia.order.common.exception.member.status;



import com.ipia.order.common.exception.ExplainError;
import com.ipia.order.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum MemberErrorStatus implements ErrorResponse {

    // 회원
    @ExplainError("회원을 찾을 수 없음")
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4001", "회원을 찾을 수 없습니다."),
    @ExplainError("이미 가입된 회원")
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER4002", "이미 가입된 회원입니다."),
    @ExplainError("약관 동의가 완료된 회원")
    MEMBER_ALREADY_SIGN_UP_COMPLETED(HttpStatus.BAD_REQUEST, "MEMBER4003", "약관 동의가 완료된 회원입니다."),
    @ExplainError("이미 탈퇴한 회원")
    ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "MEMBER4004", "이미 탈퇴한 회원입니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    MemberErrorStatus(HttpStatus httpStatus, String code, String message) {
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
