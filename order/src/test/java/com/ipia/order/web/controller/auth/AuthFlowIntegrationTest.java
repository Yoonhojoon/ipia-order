package com.ipia.order.web.controller.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("인증 플로우 통합 테스트")
class AuthFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private void register(String name, String email, String password) throws Exception {
        String body = String.format("{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", name, email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH2004"));
    }

    private Tokens loginAndGetTokens(String email, String password) throws Exception {
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH2001"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        String accessToken = root.path("data").path("accessToken").asText();
        String refreshToken = root.path("data").path("refreshToken").asText();
        return new Tokens(accessToken, refreshToken);
    }

    private Tokens refreshAndGetTokens(String refreshToken) throws Exception {
        String body = String.format("{\"token\":\"%s\"}", refreshToken);
        String response = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH2003"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        String access = root.path("data").path("accessToken").asText();
        String refresh = root.path("data").path("refreshToken").asText();
        return new Tokens(access, refresh);
    }

    @Test
    @DisplayName("회원가입 → 로그인 → 로그아웃 해피패스")
    void signup_login_logout_happy_path() throws Exception {
        String email = "flow@example.com";
        String password = "Passw0rd!";

        // 회원가입
        register("홍길동", email, password);

        // 로그인 (토큰 획득)
        Tokens tokens = loginAndGetTokens(email, password);

        // 로그아웃
        String logoutBody = String.format("{\"token\":\"%s\"}", tokens.accessToken);
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH2002"));
    }

    @Test
    @DisplayName("중복 이메일 회원가입 409")
    void register_duplicate_email_409() throws Exception {
        String email = "dupe@example.com";
        String password = "Passw0rd!";

        register("홍길동", email, password);

        String dupBody = String.format("{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", "홍길동", email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dupBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH4007"));
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 성공")
    void refresh_token_success() throws Exception {
        String email = "refreshsuccess@example.com";
        String password = "Passw0rd!";

        register("홍길동", email, password);
        Tokens tokens = loginAndGetTokens(email, password);

        Tokens refreshed = refreshAndGetTokens(tokens.refreshToken);
        assert !refreshed.accessToken().isBlank();
        assert !refreshed.refreshToken().isBlank();
    }

    @Test
    @DisplayName("무효 리프레시 토큰 재발급 401")
    void refresh_token_invalid_401() throws Exception {
        String body = "{\"token\":\"invalid-refresh-token\"}";
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH4002"));
    }

    @Test
    @DisplayName("ACCESS 토큰으로 재발급 요청 시 401")
    void refresh_with_access_token_401() throws Exception {
        String email = "accessasrefresh@example.com";
        String password = "Passw0rd!";

        register("홍길동", email, password);
        Tokens tokens = loginAndGetTokens(email, password);

        String body = String.format("{\"token\":\"%s\"}", tokens.accessToken);
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH4002"));
    }

    @Test
    @DisplayName("리프레시 토큰으로 로그아웃 요청 시 401")
    void logout_with_refresh_token_401() throws Exception {
        String email = "logoutrefresh@example.com";
        String password = "Passw0rd!";

        register("홍길동", email, password);
        Tokens tokens = loginAndGetTokens(email, password);

        String logoutBody = String.format("{\"token\":\"%s\"}", tokens.refreshToken);
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH4002"));
    }

    private record Tokens(String accessToken, String refreshToken) {}
}


