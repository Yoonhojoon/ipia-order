package com.ipia.order.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import com.ipia.order.common.exception.auth.JwtExceptionHandler;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 */
@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException(
                "JWT 시크릿 키는 최소 32바이트(256비트) 이상이어야 합니다. 현재: " + secretBytes.length + "바이트"
            );
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Access Token 생성
     * @param userId 회원 ID
     * @param email 이메일
     * @param role 권한
     * @return Access Token
     */
    public String generateAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
        
        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .claim("type", "ACCESS")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        
        log.info("액세스 토큰 생성 완료 - userId: {}, email: {}, role: {}, 만료시간: {}", 
                userId, email, role, expiryDate);
        
        return token;
    }

    /**
     * Refresh Token 생성
     * @param userId 회원 ID
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "REFRESH")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        
        log.info("리프레시 토큰 생성 완료 - userId: {}, 만료시간: {}", userId, expiryDate);
        
        return token;
    }

    /**
     * 토큰에서 회원 ID 추출
     * @param token JWT 토큰
     * @return 회원 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 토큰에서 이메일 추출
     * @param token JWT 토큰
     * @return 이메일
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("email", String.class);
    }

    /**
     * 토큰에서 권한 추출
     * @param token JWT 토큰
     * @return 권한
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 토큰 타입 확인 (ACCESS 또는 REFRESH)
     * @param token JWT 토큰
     * @return 토큰 타입
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("type", String.class);
    }

    /**
     * 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 유효 여부
     */
public void validateToken(String token) {
    try {
        Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    } catch (SecurityException | MalformedJwtException e) {
        log.info("잘못된 JWT 서명입니다.");
        throw new JwtExceptionHandler(AuthErrorStatus.INVALID_TOKEN.getMessage(), e);
    } catch (ExpiredJwtException e) {
        log.info("만료된 JWT 토큰입니다.");
        throw new JwtExceptionHandler(AuthErrorStatus.TOKEN_EXPIRED.getMessage(), e);
    } catch (UnsupportedJwtException e) {
        log.info("지원되지 않는 JWT 토큰입니다.");
        throw new JwtExceptionHandler(AuthErrorStatus.INVALID_TOKEN.getMessage(), e);
    } catch (IllegalArgumentException e) {
        log.info("JWT 토큰이 잘못되었습니다.");
        throw new JwtExceptionHandler(AuthErrorStatus.INVALID_TOKEN.getMessage(), e);
    }
}


    /**
     * 토큰에서 Claims 추출
     * @param token JWT 토큰
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
