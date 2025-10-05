package com.ipia.order.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import com.ipia.order.common.exception.general.status.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security 예외 처리 핸들러
 */
@Component
@Slf4j
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 인증 실패 시 처리 (401 Unauthorized)
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException {
        
        log.warn("인증 실패: {}", authException.getMessage());
        
        ErrorResponse errorResponse;
        if (authException instanceof BadCredentialsException) {
            errorResponse = AuthErrorStatus.LOGIN_FAILED;
        } else {
            errorResponse = AuthErrorStatus.INVALID_TOKEN;
        }
        
        sendErrorResponse(response, errorResponse);
    }

    /**
     * 권한 부족 시 처리 (403 Forbidden)
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                      AccessDeniedException accessDeniedException) throws IOException {
        
        log.warn("권한 부족: {}", accessDeniedException.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse() {
            @Override
            public HttpStatus getErrorStatus() {
                return HttpStatus.FORBIDDEN;
            }

            @Override
            public String getCode() {
                return "AUTH4007";
            }

            @Override
            public String getMessage() {
                return "접근 권한이 없습니다.";
            }
        };
        
        sendErrorResponse(response, errorResponse);
    }

    /**
     * 에러 응답 전송
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(errorResponse.getErrorStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Object> apiResponse = new ApiResponse<>(false, errorResponse.getCode(), errorResponse.getMessage(), null);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
