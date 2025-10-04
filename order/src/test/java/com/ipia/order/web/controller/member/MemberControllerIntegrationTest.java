package com.ipia.order.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.web.dto.request.MemberSignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MemberController 통합 테스트 (예외 처리 시나리오)
 */
@WebMvcTest(MemberController.class)
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("API 응답 구조 검증 - 성공 케이스")
    void apiResponseStructure_Success() throws Exception {
        // Given
        MemberSignupRequest request = MemberSignupRequest.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // ApiResponse 구조 검증
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                // MemberResponse 구조 검증
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.email").value("hong@example.com"))
                .andExpect(jsonPath("$.data.isActive").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    @DisplayName("API 응답 구조 검증 - 실패 케이스")
    void apiResponseStructure_Failure() throws Exception {
        // Given
        String invalidRequest = "{ \"name\": \"\", \"email\": \"invalid\" }";

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // ApiResponse 구조 검증
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("HTTP 헤더 검증 - Content-Type")
    void httpHeaders_ContentType() throws Exception {
        // Given
        MemberSignupRequest request = MemberSignupRequest.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    @DisplayName("HTTP 헤더 검증 - Accept")
    void httpHeaders_Accept() throws Exception {
        // Given
        Long memberId = 1L;

        // When & Then
        mockMvc.perform(get("/api/members/{id}", memberId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("쿼리 파라미터 검증")
    void queryParameters_Validation() throws Exception {
        // Given
        String name = "홍길동";

        // When & Then
        mockMvc.perform(get("/api/members/search")
                        .param("name", name)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("경로 변수 검증")
    void pathVariables_Validation() throws Exception {
        // Given
        Long memberId = 1L;

        // When & Then
        mockMvc.perform(get("/api/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.id").value(memberId));
    }

    @Test
    @DisplayName("JSON 직렬화/역직렬화 검증")
    void jsonSerialization_Validation() throws Exception {
        // Given
        MemberSignupRequest request = MemberSignupRequest.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();

        // When
        String jsonRequest = objectMapper.writeValueAsString(request);
        MemberSignupRequest deserialized = objectMapper.readValue(jsonRequest, MemberSignupRequest.class);

        // Then
        assert deserialized.getName().equals("홍길동");
        assert deserialized.getEmail().equals("hong@example.com");
    }
}
