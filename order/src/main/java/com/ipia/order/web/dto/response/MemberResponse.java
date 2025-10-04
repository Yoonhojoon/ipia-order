package com.ipia.order.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ipia.order.member.domain.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberResponse {

    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("deletedAt")
    private LocalDateTime deletedAt;

    @Builder
    private MemberResponse(Long id, String name, String email, Boolean isActive, 
                         LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /**
     * Member 엔티티를 MemberResponse로 변환
     */
    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .isActive(member.isActive())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .deletedAt(member.getDeletedAt())
                .build();
    }
}
