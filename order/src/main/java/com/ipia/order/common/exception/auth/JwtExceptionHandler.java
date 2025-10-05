package com.ipia.order.common.exception.auth;

import io.jsonwebtoken.JwtException;

/**
 * JWT 관련 예외를 처리하는 핸들러
 * JwtException을 상속받아 JWT 관련 예외를 통일된 형태로 처리
 */
public class JwtExceptionHandler extends JwtException {
    
    public JwtExceptionHandler(String message) {
        super(message);
    }

    public JwtExceptionHandler(String message, Throwable cause) {
        super(message, cause);
    }
}
