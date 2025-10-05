package com.ipia.order.common.filter;

import com.ipia.order.common.exception.auth.AuthHandler;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import com.ipia.order.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증 필터
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractTokenFromRequest(request);
        
        if (token != null && !isPublicEndpoint(request)) {
            try {
                // 토큰 유효성 검증
                if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                    // 토큰에서 사용자 정보 추출
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String email = jwtUtil.getEmailFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);
                    
                    // Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                    
                    // SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("JWT 인증 성공: userId={}, email={}, role={}", userId, email, role);
                } else {
                    log.warn("유효하지 않은 JWT 토큰: {}", token);
                    throw new AuthHandler(AuthErrorStatus.INVALID_TOKEN);
                }
            } catch (Exception e) {
                log.error("JWT 토큰 처리 중 오류 발생", e);
                throw new AuthHandler(AuthErrorStatus.INVALID_TOKEN);
            }
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 공개 엔드포인트 확인
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI.startsWith("/api/auth/login") ||
               requestURI.startsWith("/api/auth/signup") ||
               requestURI.startsWith("/h2-console") ||
               requestURI.startsWith("/actuator");
    }
}
