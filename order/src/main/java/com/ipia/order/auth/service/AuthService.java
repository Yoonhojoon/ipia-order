package com.ipia.order.auth.service;

import com.ipia.order.member.domain.Member;
import com.ipia.order.web.dto.response.auth.LoginResponse;

public interface AuthService {
    
    /**
     * 로그인
     * @param email 이메일
     * @param password 비밀번호
     * @return JWT 토큰 (임시로 String 반환)
     */
    // removed legacy login(email,password)
    
    /**
     * 로그아웃
     * @param token JWT 토큰
     */
    void logout(String token);

    /**
     * 회원가입
     * @param name 이름
     * @param email 이메일
     * @param password 평문 비밀번호
     * @return 생성된 회원
     */
    Member register(String name, String email, String password);

    /**
     * 로그인 (Access/Refresh 토큰 발급 포함)
     * @param email 이메일
     * @param password 비밀번호
     * @return 로그인 결과(토큰 및 사용자 정보)
     */
    LoginResponse login(String email, String password);

    /**
     * 리프레시 토큰으로 액세스/리프레시 재발급
     * @param refreshToken 리프레시 토큰
     * @return 새 토큰과 사용자 정보
     */
    LoginResponse refresh(String refreshToken);
}
