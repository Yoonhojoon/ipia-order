package com.ipia.order.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.common.exception.ApiResponse;
import com.ipia.order.common.exception.auth.JwtExceptionHandler;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 관련 예외를 처리하는 필터
 * JwtAuthenticationFilter에서 발생하는 JwtExceptionHandler를 처리
 */
@Component
@Slf4j
@Order(0)
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 다음 필터(JwtAuthenticationFilter)로 요청 전달
            filterChain.doFilter(request, response);
            
        } catch (JwtExceptionHandler e) {
            log.warn("JWT 예외 발생: {}", e.getMessage());
            
            // JWT 예외를 통일된 응답으로 변환
            handleJwtException(response, e);
        }
    }

    /**
     * JWT 예외를 처리하여 JSON 응답으로 반환
     */
    private void handleJwtException(HttpServletResponse response, JwtExceptionHandler e) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 예외 메시지에 따라 적절한 AuthErrorStatus 선택
        AuthErrorStatus errorStatus = determineErrorStatus(e.getMessage());
        
        ApiResponse<Object> apiResponse = new ApiResponse<>(
            false, 
            errorStatus.getCode(), 
            errorStatus.getMessage(), 
            null
        );
        
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * 예외 메시지에 따라 적절한 AuthErrorStatus 결정
     */
    private AuthErrorStatus determineErrorStatus(String message) {
        if (message.contains("만료")) {
            return AuthErrorStatus.TOKEN_EXPIRED;
        } else {
            return AuthErrorStatus.INVALID_TOKEN;
        }
    }
}
