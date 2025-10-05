package com.ipia.order.web.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.enums.MemberRole;
import com.ipia.order.web.controller.TestConfig;
import com.ipia.order.auth.service.AuthService;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.common.util.JwtUtil;
import com.ipia.order.web.dto.response.auth.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(TestConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;
    
    @MockitoBean
    MemberService memberService;
    
    @MockitoBean
    JwtUtil jwtUtil;

    @Nested
    @DisplayName("/api/auth/login")
    class LoginApi {
        @Test
        @DisplayName("이메일/비밀번호 입력 시 200을 기대한다")
        void login_shouldReturn200_whenValid() throws Exception {
            // Given
            when(authService.login("test@example.com", "pass1234"))
                .thenReturn(new LoginResponse(
                    "access-token",
                    "refresh-token",
                    1L,
                    "test@example.com",
                    "USER"
                ));
            
            String body = "{\"email\":\"test@example.com\",\"password\":\"pass1234\"}";
            
            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("AUTH2001"))
                    .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
        }

        @Test
        @DisplayName("잘못된 자격 증명 시 401을 기대한다")
        void login_shouldReturn401_whenBadCredentials() throws Exception {
            // Given
            doThrow(new RuntimeException("Login failed")).when(authService).login("test@example.com", "wrong");
            
            String body = "{\"email\":\"test@example.com\",\"password\":\"wrong\"}";
            
            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("AUTH4001"));
        }

        @Test
        @DisplayName("검증 실패 시 400을 기대한다")
        void login_shouldReturn400_whenValidationFails() throws Exception {
            String body = "{\"email\":\"not-an-email\",\"password\":\"\"}";
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }

    @Nested
    @DisplayName("/api/auth/refresh")
    class RefreshApi {
        @Test
        @DisplayName("유효한 리프레시 토큰 시 200을 기대한다")
        void refresh_shouldReturn200_whenValidToken() throws Exception {
            // Given
            when(authService.refresh("valid-refresh-token")).thenReturn(
                new LoginResponse(
                    "new-access-token",
                    "new-refresh-token",
                    1L,
                    "test@example.com",
                    "USER"
                )
            );
            
            String body = "{\"token\":\"valid-refresh-token\"}";
            
            // When & Then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("AUTH2003"));
        }

        @Test
        @DisplayName("만료/무효 토큰 시 401을 기대한다")
        void refresh_shouldReturn401_whenInvalidToken() throws Exception {
            // Given
            doThrow(new RuntimeException("Invalid token")).when(authService).refresh("invalid-refresh-token");
            
            String body = "{\"token\":\"invalid-refresh-token\"}";
            
            // When & Then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("AUTH4002"));
        }
    }

    @Nested
    @DisplayName("/api/auth/logout")
    class LogoutApi {
        @Test
        @DisplayName("정상 로그아웃 시 200을 기대한다")
        void logout_shouldReturn200_whenValid() throws Exception {
            // Given - logout 메서드는 void이므로 아무것도 설정하지 않음
            
            String body = "{\"token\":\"access-token\"}";
            
            // When & Then
            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("AUTH2002"));
        }
    }

    @Nested
    @DisplayName("/api/auth/register")
    class RegisterApi {
        @Test
        @DisplayName("정상 회원가입 시 200을 기대한다")
        void register_shouldReturn200_whenValid() throws Exception {
            // Given
            when(authService.register(eq("홍길동"), eq("test@example.com"), eq("pass1234")))
                .thenReturn(Member.createTestMember(
                        1L, "홍길동", "test@example.com", "encoded", 
                        MemberRole.USER
                    )
                );

            String body = "{\"name\":\"홍길동\",\"email\":\"test@example.com\",\"password\":\"pass1234\"}";

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("AUTH2004"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        @DisplayName("중복 이메일 시 409를 기대한다")
        void register_shouldReturn409_whenDuplicateEmail() throws Exception {
            // Given
            doThrow(new RuntimeException("duplicate")).when(authService)
                .register(eq("홍길동"), eq("dupe@example.com"), eq("pass1234"));

            String body = "{\"name\":\"홍길동\",\"email\":\"dupe@example.com\",\"password\":\"pass1234\"}";

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("AUTH4007"));
        }

        @Test
        @DisplayName("검증 실패 시 400을 기대한다")
        void register_shouldReturn400_whenValidationFails() throws Exception {
            String body = "{\"name\":\"\",\"email\":\"not-email\",\"password\":\"123\"}";
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }
    @Nested
    @DisplayName("에러 응답 포맷")
    class ErrorFormat {
        @Test
        @DisplayName("컨트롤러 예외 시 ApiResponse 포맷을 기대한다")
        void errorResponse_shouldMatchApiResponseFormat() throws Exception {
            // Given
            doThrow(new RuntimeException("Login failed")).when(authService).login("test@example.com", "pass1234");
            
            String body = "{\"email\":\"test@example.com\",\"password\":\"pass1234\"}";
            
            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.isSuccess").isBoolean())
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}


