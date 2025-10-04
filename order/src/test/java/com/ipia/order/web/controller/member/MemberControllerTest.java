package com.ipia.order.web.controller.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipia.order.common.exception.member.MemberHandler;
import com.ipia.order.common.exception.member.status.MemberErrorStatus;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.service.MemberService;
import com.ipia.order.web.dto.request.MemberPasswordRequest;
import com.ipia.order.web.dto.request.MemberSignupRequest;
import com.ipia.order.web.dto.request.MemberUpdateRequest;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import java.util.Optional;

/**
 * MemberController MockMvc 테스트
 */
@WebMvcTest(value = MemberController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        // 공통 Mock 설정
        Member mockMember = createMockMember(1L, "홍길동", "hong@example.com");
        Mockito.when(memberService.signup(Mockito.anyString(), Mockito.anyString())).thenReturn(mockMember);
        Mockito.when(memberService.findById(Mockito.eq(1L))).thenReturn(Optional.of(mockMember));
        Mockito.when(memberService.findById(Mockito.eq(999L))).thenReturn(Optional.empty());
        Mockito.when(memberService.findByEmail(Mockito.anyString())).thenReturn(Optional.of(mockMember));
        Mockito.when(memberService.findAll()).thenReturn(List.of(mockMember));
        Mockito.when(memberService.findByName(Mockito.anyString())).thenReturn(List.of(mockMember));
        // updateNickname은 새로운 이름으로 업데이트된 Member 객체를 반환해야 함
        Mockito.when(memberService.updateNickname(Mockito.eq(1L), Mockito.anyString())).thenAnswer(invocation -> {
            String newName = invocation.getArgument(1);
            return createMockMember(1L, newName, "hong@example.com");
        });
        Mockito.when(memberService.updateNickname(Mockito.eq(999L), Mockito.anyString())).thenThrow(new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
        Mockito.doNothing().when(memberService).updatePassword(Mockito.eq(1L), Mockito.eq("oldPassword123"), Mockito.anyString());
        Mockito.doThrow(new MemberHandler(MemberErrorStatus.PASSWORD_MISMATCH)).when(memberService).updatePassword(Mockito.eq(1L), Mockito.eq("wrongPassword"), Mockito.anyString());
        Mockito.doNothing().when(memberService).withdraw(Mockito.eq(1L));
        Mockito.doThrow(new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND)).when(memberService).withdraw(Mockito.eq(999L));
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 성공")
    void signup_Success() throws Exception {
        // Given
        MemberSignupRequest request = createSignupRequest("홍길동", "hong@example.com");
        String jsonRequest = createJsonRequest(request);

        // When & Then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.email").value("hong@example.com"));
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 실패 (잘못된 입력값)")
    void signup_Failure_InvalidInput() throws Exception {
        // Given
        MemberSignupRequest request = createSignupRequest("", "invalid-email");
        String jsonRequest = createJsonRequest(request);

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
    @DisplayName("회원 조회 API 테스트 - 성공")
    void findById_Success() throws Exception {
        // Given
        Long memberId = 1L;

        // When & Then
        mockMvc.perform(get("/api/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(memberId));
    }

    @Test
    @DisplayName("회원 조회 API 테스트 - 실패 (존재하지 않는 회원)")
    void findById_Failure_NotFound() throws Exception {
        // Given
        Long memberId = 999L;

        // When & Then
        mockMvc.perform(get("/api/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("이메일로 회원 조회 API 테스트 - 성공")
    void findByEmail_Success() throws Exception {
        // Given
        String email = "hong@example.com";

        // When & Then
        mockMvc.perform(get("/api/members/email/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.email").value(email));
    }

    @Test
    @DisplayName("전체 회원 조회 API 테스트 - 성공")
    void findAll_Success() throws Exception {
        // Given
        // When & Then
        mockMvc.perform(get("/api/members")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("이름으로 회원 조회 API 테스트 - 성공")
    void findByName_Success() throws Exception {
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
    @DisplayName("회원 정보 수정 API 테스트 - 성공")
    void update_Success() throws Exception {
        // Given
        Long memberId = 1L;
        MemberUpdateRequest request = createUpdateRequest("김철수");
        String jsonRequest = createJsonRequest(request);

        // When & Then
        mockMvc.perform(put("/api/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("김철수"));
    }

    @Test
    @DisplayName("회원 정보 수정 API 테스트 - 실패 (잘못된 입력값)")
    void update_Failure_InvalidInput() throws Exception {
        // Given
        Long memberId = 1L;
        MemberUpdateRequest request = createUpdateRequest("");
        String jsonRequest = createJsonRequest(request);

        // When & Then
        mockMvc.perform(put("/api/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("비밀번호 변경 API 테스트 - 성공")
    void updatePassword_Success() throws Exception {
        // Given
        Long memberId = 1L;
        MemberPasswordRequest request = createPasswordRequest("oldPassword123", "newPassword456");
        String jsonRequest = createJsonRequest(request);

        // When & Then
        mockMvc.perform(put("/api/members/{id}/password", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    @Test
    @DisplayName("비밀번호 변경 API 테스트 - 실패 (잘못된 현재 비밀번호)")
    void updatePassword_Failure_InvalidCurrentPassword() throws Exception {
        // Given
        Long memberId = 1L;
        MemberPasswordRequest request = createPasswordRequest("wrongPassword", "newPassword456");
        String jsonRequest = createJsonRequest(request);

        // When & Then
        mockMvc.perform(put("/api/members/{id}/password", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("회원 탈퇴 API 테스트 - 성공")
    void withdraw_Success() throws Exception {
        // Given
        Long memberId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    @Test
    @DisplayName("회원 탈퇴 API 테스트 - 실패 (존재하지 않는 회원)")
    void withdraw_Failure_NotFound() throws Exception {
        // Given
        Long memberId = 999L;

        // When & Then
        mockMvc.perform(delete("/api/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    // === 헬퍼 메서드들 ===

    /**
     * JSON 요청 본문 생성
     */
    private String createJsonRequest(Object request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }

    /**
     * 회원가입 요청 DTO 생성
     */
    private MemberSignupRequest createSignupRequest(String name, String email) {
        return MemberSignupRequest.builder()
                .name(name)
                .email(email)
                .build();
    }

    /**
     * 회원 수정 요청 DTO 생성
     */
    private MemberUpdateRequest createUpdateRequest(String name) {
        return MemberUpdateRequest.builder()
                .name(name)
                .build();
    }

    /**
     * 비밀번호 변경 요청 DTO 생성
     */
    private MemberPasswordRequest createPasswordRequest(String currentPassword, String newPassword) {
        return MemberPasswordRequest.builder()
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .build();
    }

    /**
     * Mock Member 엔티티 생성
     */
    private Member createMockMember(Long id, String name, String email) {
        Member member = Member.builder()
                .name(name)
                .email(email)
                .build();
        
        // Reflection을 사용하여 ID 설정 (테스트용)
        try {
            java.lang.reflect.Field idField = Member.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(member, id);
        } catch (Exception e) {
            // 테스트용이므로 예외 무시
        }
        
        return member;
    }
}
