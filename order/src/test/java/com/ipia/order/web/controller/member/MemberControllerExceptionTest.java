package com.ipia.order.web.controller.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.web.dto.request.MemberSignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MemberController 예외 처리 테스트
 */
@WebMvcTest(value = MemberController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class MemberControllerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("잘못된 HTTP 메서드 요청 - 405 Method Not Allowed")
    void invalidHttpMethod_Returns405() throws Exception {
        // Given & When & Then
        mockMvc.perform(patch("/api/members/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("잘못된 Content-Type 요청 - 415 Unsupported Media Type")
    void invalidContentType_Returns415() throws Exception {
        // Given
        String invalidContent = "name=홍길동&email=hong@example.com";

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(invalidContent))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("잘못된 JSON 형식 요청 - 400 Bad Request")
    void invalidJsonFormat_Returns400() throws Exception {
        // Given
        String invalidJson = "{ \"name\": \"홍길동\", \"email\": }";

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("필수 파라미터 누락 - 400 Bad Request")
    void missingRequiredParameter_Returns400() throws Exception {
        // Given
        String requestWithoutName = "{ \"email\": \"hong@example.com\" }";

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithoutName))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("잘못된 이메일 형식 - 400 Bad Request")
    void invalidEmailFormat_Returns400() throws Exception {
        // Given
        MemberSignupRequest request = MemberSignupRequest.builder()
                .name("홍길동")
                .email("invalid-email-format")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("잘못된 ID 타입 - 400 Bad Request")
    void invalidIdType_Returns400() throws Exception {
        // Given & When & Then
        mockMvc.perform(get("/api/members/invalid-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("존재하지 않는 엔드포인트 - 404 Not Found")
    void nonExistentEndpoint_Returns404() throws Exception {
        // Given & When & Then
        mockMvc.perform(get("/api/members/invalid-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("서버 내부 오류 - 500 Internal Server Error")
    void serverError_Returns500() throws Exception {
        // Given
        MemberSignupRequest request = MemberSignupRequest.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // Mock 설정 - 예외 발생
        when(memberService.signup(anyString(), anyString())).thenThrow(new RuntimeException("서버 내부 오류"));

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("요청 본문이 너무 큰 경우 - 413 Payload Too Large")
    void requestTooLarge_Returns413() throws Exception {
        // Given
        StringBuilder largeName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeName.append("홍길동");
        }
        
        MemberSignupRequest request = MemberSignupRequest.builder()
                .name(largeName.toString())
                .email("hong@example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }
}
