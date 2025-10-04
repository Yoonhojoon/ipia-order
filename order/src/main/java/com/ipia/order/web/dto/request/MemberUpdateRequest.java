package com.ipia.order.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 정보 수정 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberUpdateRequest {

    @NotBlank(message = "회원 이름은 필수입니다")
    @Size(max = 100, message = "회원 이름은 100자를 초과할 수 없습니다")
    private String name;

    @Builder
    private MemberUpdateRequest(String name) {
        this.name = name;
    }
}
