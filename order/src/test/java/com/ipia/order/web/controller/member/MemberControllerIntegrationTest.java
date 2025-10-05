package com.ipia.order.web.controller.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.member.domain.Member;
import com.ipia.order.common.enums.MemberRole;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MemberController 통합 테스트 (예외 처리 시나리오)
 */
@WebMvcTest(value = MemberController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class   MemberControllerIntegrationTest {

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

        // Mock 설정
        Member mockMember = Member.createTestMember(1L, "홍길동", "hong@example.com", "encodedPassword", MemberRole.USER);
        when(memberService.signup(anyString(), anyString())).thenReturn(mockMember);

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
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
                .andExpect(jsonPath("$.data").exists());
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

        // Mock 설정
        Member mockMember = Member.createTestMember(1L, "홍길동", "hong@example.com", "encodedPassword", MemberRole.USER);
        when(memberService.signup(anyString(), anyString())).thenReturn(mockMember);

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    @DisplayName("HTTP 헤더 검증 - Accept")
    void httpHeaders_Accept() throws Exception {
        // Given
        Long memberId = 1L;

        // Mock 설정
        Member mockMember = Member.createTestMember(1L, "홍길동", "hong@example.com", "encodedPassword", MemberRole.USER);
        when(memberService.findById(anyLong())).thenReturn(Optional.of(mockMember));

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

        // Mock 설정
        Member mockMember = Member.createTestMember(1L, "홍길동", "hong@example.com", "encodedPassword", MemberRole.USER);
        when(memberService.findByName(anyString())).thenReturn(Arrays.asList(mockMember));

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
