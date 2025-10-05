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
    ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "MEMBER4004", "이미 탈퇴한 회원입니다."),

    // 입력/정책 위반 관련
    @ExplainError("유효하지 않은 입력 값")
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "MEMBER4005", "유효하지 않은 입력 값입니다."),
    @ExplainError("닉네임이 비어있음 또는 공백")
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "MEMBER4006", "닉네임은 비어있을 수 없습니다."),
    @ExplainError("현재 비밀번호 불일치")
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER4007", "현재 비밀번호가 일치하지 않습니다."),
    @ExplainError("새 비밀번호 정책 위반")
    PASSWORD_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "MEMBER4008", "새 비밀번호가 정책을 위반했습니다."),
    @ExplainError("권한이 올바르지 않은 회원")
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "MEMBER4009", "유효하지 않은 권한 코드입니다.");




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
