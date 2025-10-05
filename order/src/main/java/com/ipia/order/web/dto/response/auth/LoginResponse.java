package com.ipia.order.web.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long memberId;
    private String email;
    private String role;
}


