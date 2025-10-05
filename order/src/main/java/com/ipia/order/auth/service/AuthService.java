package com.ipia.order.auth.service;

import com.ipia.order.member.domain.Member;

public interface AuthService {
    
    /**
     * 로그인
     * @param email 이메일
     * @param password 비밀번호
     * @return JWT 토큰 (임시로 String 반환)
     */
    String login(String email, String password);
    
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
}
