package com.ipia.order.web.controller.auth;

import com.ipia.order.auth.service.AuthService;
import com.ipia.order.common.exception.ApiErrorCodeExample;
import com.ipia.order.common.exception.ApiErrorCodeExamples;
import com.ipia.order.common.exception.ApiResponse;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import com.ipia.order.common.exception.auth.status.AuthSuccessStatus;
import com.ipia.order.common.util.JwtUtil;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.web.dto.request.auth.LoginRequest;
import com.ipia.order.web.dto.request.auth.RegisterRequest;
import com.ipia.order.web.dto.request.auth.TokenRequest;
import com.ipia.order.web.dto.response.MemberResponse;
import com.ipia.order.web.dto.response.auth.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 관리", description = "로그인, 로그아웃, 토큰 갱신 등의 인증 관련 API")
public class AuthController {
    
    private final AuthService authService;
    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    /**
     * 로그인
     * POST /api/auth/login
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공", 
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 실패")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = AuthErrorStatus.class, codes = {"LOGIN_FAILED"})
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request.getEmail(), request.getPassword());
            return ApiResponse.onSuccess(AuthSuccessStatus.LOGIN_SUCCESS, response);
        } catch (Exception e) {
            return ApiResponse.onFailure(AuthErrorStatus.LOGIN_FAILED, (LoginResponse) null);
        }
    }

    /**
     * 회원가입
     * POST /api/auth/register
     */
    @Operation(summary = "회원가입", description = "이름, 이메일, 비밀번호로 회원가입을 처리합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = com.ipia.order.web.dto.response.MemberResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 회원")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = AuthErrorStatus.class, codes = {"MEMBER_ALREADY_EXISTS"})
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<com.ipia.order.web.dto.response.MemberResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            Member newMember = authService.register(request.getName(), request.getEmail(), request.getPassword());
            MemberResponse response = MemberResponse.from(newMember);
            return ApiResponse.onSuccess(AuthSuccessStatus.REGISTER_SUCCESS, response);
        } catch (Exception e) {
            return ApiResponse.onFailure(AuthErrorStatus.MEMBER_ALREADY_EXISTS, (MemberResponse) null);
        }
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @Operation(summary = "로그아웃", description = "JWT 토큰을 무효화하여 로그아웃을 처리합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = AuthErrorStatus.class, codes = {"INVALID_TOKEN"})
    })
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
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공", 
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "비활성화된 회원")
    })
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = AuthErrorStatus.class, codes = {"INVALID_TOKEN", "INACTIVE_MEMBER"})
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody TokenRequest request) {
        try {
            LoginResponse response = authService.refresh(request.getToken());
            return ApiResponse.onSuccess(AuthSuccessStatus.TOKEN_REFRESH_SUCCESS, response);
        } catch (Exception e) {
            return ApiResponse.onFailure(AuthErrorStatus.INVALID_TOKEN, (LoginResponse) null);
        }
    }
}


