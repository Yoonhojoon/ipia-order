package com.ipia.order.auth.service;

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
}
