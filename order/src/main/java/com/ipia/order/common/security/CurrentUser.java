package com.ipia.order.common.security;

public class CurrentUser {
    private final Long memberId;
    private final String email;
    private final String role;

    public CurrentUser(Long memberId, String email, String role) {
        this.memberId = memberId;
        this.email = email;
        this.role = role;
    }

    public Long getMemberId() { return memberId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}


