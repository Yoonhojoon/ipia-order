package com.ipia.order.web.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.web.controller.TestConfig;
import com.ipia.order.auth.service.AuthService;
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

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(TestConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @Nested
    @DisplayName("/api/auth/login")
    class LoginApi {
        @Test
        @DisplayName("이메일/비밀번호 입력 시 200을 기대한다 (레드: 아직 구현 전)")
        void login_shouldReturn200_whenValid() throws Exception {
            String body = "{\"email\":\"test@example.com\",\"password\":\"pass1234\"}";
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("잘못된 자격 증명 시 401을 기대한다 (레드)")
        void login_shouldReturn401_whenBadCredentials() throws Exception {
            String body = "{\"email\":\"test@example.com\",\"password\":\"wrong\"}";
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("검증 실패 시 400을 기대한다 (레드)")
        void login_shouldReturn400_whenValidationFails() throws Exception {
            String body = "{\"email\":\"not-an-email\",\"password\":\"\"}";
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("/api/auth/refresh")
    class RefreshApi {
        @Test
        @DisplayName("유효한 리프레시 토큰 시 200을 기대한다 (레드)")
        void refresh_shouldReturn200_whenValidToken() throws Exception {
            String body = "{\"token\":\"valid-refresh-token\"}";
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("만료/무효 토큰 시 401을 기대한다 (레드)")
        void refresh_shouldReturn401_whenInvalidToken() throws Exception {
            String body = "{\"token\":\"invalid-refresh-token\"}";
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("/api/auth/logout")
    class LogoutApi {
        @Test
        @DisplayName("정상 로그아웃 시 200을 기대한다 (레드)")
        void logout_shouldReturn200_whenValid() throws Exception {
            String body = "{\"token\":\"access-token\"}";
            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("에러 응답 포맷")
    class ErrorFormat {
        @Test
        @DisplayName("컨트롤러 예외 시 ApiResponse 포맷을 기대한다 (레드)")
        void errorResponse_shouldMatchApiResponseFormat() throws Exception {
            String body = "{\"email\":\"test@example.com\",\"password\":\"pass1234\"}";
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().is4xxClientError())
                    .andExpect(jsonPath("$.isSuccess").isBoolean())
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.message").exists());
        }
    }
}


