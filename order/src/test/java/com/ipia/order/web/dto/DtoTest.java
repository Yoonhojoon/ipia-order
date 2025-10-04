package com.ipia.order.web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ipia.order.web.dto.request.MemberPasswordRequest;
import com.ipia.order.web.dto.request.MemberSignupRequest;
import com.ipia.order.web.dto.request.MemberUpdateRequest;
import com.ipia.order.web.dto.response.MemberResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DTO 직렬화/역직렬화 테스트
 */
class DtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("MemberSignupRequest JSON 직렬화/역직렬화 테스트")
    void memberSignupRequest_Serialization() throws Exception {
        // Given
        MemberSignupRequest request = MemberSignupRequest.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        MemberSignupRequest deserialized = objectMapper.readValue(json, MemberSignupRequest.class);

        // Then
        assertThat(deserialized.getName()).isEqualTo("홍길동");
        assertThat(deserialized.getEmail()).isEqualTo("hong@example.com");
    }

    @Test
    @DisplayName("MemberUpdateRequest JSON 직렬화/역직렬화 테스트")
    void memberUpdateRequest_Serialization() throws Exception {
        // Given
        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .name("김철수")
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        MemberUpdateRequest deserialized = objectMapper.readValue(json, MemberUpdateRequest.class);

        // Then
        assertThat(deserialized.getName()).isEqualTo("김철수");
    }

    @Test
    @DisplayName("MemberPasswordRequest JSON 직렬화/역직렬화 테스트")
    void memberPasswordRequest_Serialization() throws Exception {
        // Given
        MemberPasswordRequest request = MemberPasswordRequest.builder()
                .currentPassword("oldPassword123")
                .newPassword("newPassword456")
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);
        MemberPasswordRequest deserialized = objectMapper.readValue(json, MemberPasswordRequest.class);

        // Then
        assertThat(deserialized.getCurrentPassword()).isEqualTo("oldPassword123");
        assertThat(deserialized.getNewPassword()).isEqualTo("newPassword456");
    }

    @Test
    @DisplayName("MemberResponse JSON 직렬화/역직렬화 테스트")
    void memberResponse_Serialization() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        MemberResponse response = MemberResponse.builder()
                .id(1L)
                .name("홍길동")
                .email("hong@example.com")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();

        // When
        String json = objectMapper.writeValueAsString(response);
        MemberResponse deserialized = objectMapper.readValue(json, MemberResponse.class);

        // Then
        assertThat(deserialized.getId()).isEqualTo(1L);
        assertThat(deserialized.getName()).isEqualTo("홍길동");
        assertThat(deserialized.getEmail()).isEqualTo("hong@example.com");
        assertThat(deserialized.getIsActive()).isTrue();
        assertThat(deserialized.getCreatedAt()).isNotNull();
        assertThat(deserialized.getUpdatedAt()).isNotNull();
        assertThat(deserialized.getDeletedAt()).isNull();
    }
}
