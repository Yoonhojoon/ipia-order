package com.ipia.order.web.controller.auth;

import com.ipia.order.auth.service.AuthService;
import com.ipia.order.common.exception.ApiResponse;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import com.ipia.order.common.exception.auth.status.AuthSuccessStatus;
import com.ipia.order.common.util.JwtUtil;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.web.dto.request.auth.LoginRequest;
import com.ipia.order.web.dto.request.auth.TokenRequest;
import com.ipia.order.web.dto.response.auth.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // AuthService를 통한 로그인 처리
            String accessToken = authService.login(request.getEmail(), request.getPassword());
            
            // 토큰에서 사용자 정보 추출
            Long memberId = jwtUtil.getUserIdFromToken(accessToken);
            String email = jwtUtil.getEmailFromToken(accessToken);
            String role = jwtUtil.getRoleFromToken(accessToken);
            
            // Refresh Token 생성
            String refreshToken = jwtUtil.generateRefreshToken(memberId);
            
            // 응답 DTO 생성
            LoginResponse response = new LoginResponse(
                accessToken,
                refreshToken,
                memberId,
                email,
                role
            );
            
            return ApiResponse.onSuccess(AuthSuccessStatus.LOGIN_SUCCESS, response);
            
        } catch (Exception e) {
            return ApiResponse.onFailure(AuthErrorStatus.LOGIN_FAILED, (LoginResponse) null);
        }
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRequest request) {
        try {
            authService.logout(request.getToken());
            return ApiResponse.onSuccess(AuthSuccessStatus.LOGOUT_SUCCESS);
        } catch (Exception e) {
            return ApiResponse.onFailure(AuthErrorStatus.INVALID_TOKEN, null);
        }
    }

    /**
     * 토큰 갱신
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody TokenRequest request) {
        try {
            // Refresh Token 검증
            jwtUtil.validateToken(request.getToken());
            
            // 토큰 타입 확인
            String tokenType = jwtUtil.getTokenType(request.getToken());
            if (!"REFRESH".equals(tokenType)) {
                return ApiResponse.onFailure(AuthErrorStatus.INVALID_TOKEN, (LoginResponse) null);
            }
            
            // 토큰에서 사용자 ID 추출
            Long memberId = jwtUtil.getUserIdFromToken(request.getToken());
            
            // 회원 정보 조회
            Member member = memberService.findById(memberId).orElse(null);
            if (member == null || !member.isActive()) {
                return ApiResponse.onFailure(AuthErrorStatus.INACTIVE_MEMBER, (LoginResponse) null);
            }
            
            // 새로운 Access Token 생성
            String newAccessToken = jwtUtil.generateAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole().getCode()
            );
            
            // 새로운 Refresh Token 생성
            String newRefreshToken = jwtUtil.generateRefreshToken(memberId);
            
            // 응답 DTO 생성
            LoginResponse response = new LoginResponse(
                newAccessToken,
                newRefreshToken,
                member.getId(),
                member.getEmail(),
                member.getRole().getCode()
            );
            
            return ApiResponse.onSuccess(AuthSuccessStatus.TOKEN_REFRESH_SUCCESS, response);
            
        } catch (Exception e) {
            return ApiResponse.onFailure(AuthErrorStatus.INVALID_TOKEN, (LoginResponse) null);
        }
    }
}


