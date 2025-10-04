package com.ipia.order.common.exception.member.status;



import com.ipia.order.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum MemberSuccessStatus implements SuccessResponse {

    // 로그인/회원가입
    SIGN_IN_SUCCESS(HttpStatus.OK, "MEMBER2000", "로그인이 완료되었습니다."),
    SIGN_UP_SUCCESS(HttpStatus.CREATED, "MEMBER2001", "회원가입이 완료되었습니다."),
    SIGN_OUT_SUCCESS(HttpStatus.OK, "MEMBER2002", "로그아웃이 완료되었습니다."),
    
    // 회원 조회
    MEMBER_FOUND(HttpStatus.OK, "MEMBER2003", "회원 정보를 성공적으로 조회했습니다."),
    MEMBERS_FOUND(HttpStatus.OK, "MEMBER2004", "회원 목록을 성공적으로 조회했습니다."),
    
    // 회원 정보 수정
    MEMBER_UPDATED(HttpStatus.OK, "MEMBER2005", "회원 정보가 성공적으로 수정되었습니다."),
    PASSWORD_UPDATED(HttpStatus.OK, "MEMBER2006", "비밀번호가 성공적으로 변경되었습니다."),
    
    // 회원 탈퇴
    MEMBER_WITHDRAWN(HttpStatus.OK, "MEMBER2007", "회원 탈퇴가 완료되었습니다.");



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
