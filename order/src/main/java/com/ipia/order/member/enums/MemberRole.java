package com.ipia.order.member.enums;

import com.ipia.order.common.exception.member.MemberHandler;
import com.ipia.order.common.exception.member.status.MemberErrorStatus;

/**
 * 회원 권한 열거형
 */
public enum MemberRole {
    USER("USER", "일반 사용자"),
    ADMIN("ADMIN", "관리자"),
    SELLER("SELLER", "판매자");

    private final String code;
    private final String description;

    MemberRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 코드로 MemberRole 찾기
     * @param code 권한 코드
     * @return MemberRole
     * @throws IllegalArgumentException 유효하지 않은 코드인 경우
     */
    public static MemberRole fromCode(String code) {
        for (MemberRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new MemberHandler(MemberErrorStatus.INVALID_ROLE);
    }
}
