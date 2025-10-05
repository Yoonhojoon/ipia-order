package com.ipia.order.web.controller.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.enums.MemberRole;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.common.util.JwtUtil;
// signup 관련 테스트는 Auth로 이전됨
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

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

    @MockitoBean
    private JwtUtil jwtUtil;

    // 회원가입 성공 케이스는 AuthController로 이전됨

    @Test
    @DisplayName("API 응답 구조 검증 - 실패 케이스 (PUT /api/members/{id})")
    void apiResponseStructure_Failure() throws Exception {
        // Given
        String invalidRequest = "{ }";

        // When & Then
        mockMvc.perform(put("/api/members/{id}", 1)
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
    @DisplayName("HTTP 헤더 검증 - Content-Type (PUT /api/members/{id})")
    void httpHeaders_ContentType() throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(java.util.Map.of("name", "홍길동"));
        Member updated = Member.createTestMember(1L, "홍길동", "hong@example.com", "encodedPassword", MemberRole.USER);
        when(memberService.updateNickname(anyLong(), anyString())).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/members/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
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


    // 회원가입 DTO 직렬화 테스트는 Auth 쪽으로 이전
}
