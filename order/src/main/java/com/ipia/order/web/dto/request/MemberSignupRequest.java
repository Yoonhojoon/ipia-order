package com.ipia.order.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSignupRequest {

    @NotBlank(message = "회원 이름은 필수입니다")
    @Size(max = 100, message = "회원 이름은 100자를 초과할 수 없습니다")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 200, message = "이메일은 200자를 초과할 수 없습니다")
    private String email;

    @Builder
    private MemberSignupRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
