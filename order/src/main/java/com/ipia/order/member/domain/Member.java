package com.ipia.order.member.domain;

import java.time.LocalDateTime;

import org.springframework.util.StringUtils;

import com.ipia.order.common.entity.BaseEntity;
import com.ipia.order.common.exception.member.MemberHandler;
import com.ipia.order.common.exception.member.status.MemberErrorStatus;
import com.ipia.order.member.enums.MemberRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members", uniqueConstraints = {
    @UniqueConstraint(name = "uk_members_email", columnNames = "email")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role = MemberRole.USER;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Member(String name, String email, String password, MemberRole role) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);
        this.name = name;
        this.email = email;
        this.password = password; // 이미 암호화된 비밀번호를 받음
        this.role = role != null ? role : MemberRole.USER;
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new MemberHandler(MemberErrorStatus.NAME_REQUIRED);
        }
        if (name.length() > 100) {
            throw new MemberHandler(MemberErrorStatus.NAME_TOO_LONG);
        }
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new MemberHandler(MemberErrorStatus.EMAIL_REQUIRED);
        }
        if (email.length() > 200) {
            throw new MemberHandler(MemberErrorStatus.EMAIL_TOO_LONG);
        }
        // 간단한 이메일 형식 검증
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new MemberHandler(MemberErrorStatus.EMAIL_FORMAT_INVALID);
        }
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new MemberHandler(MemberErrorStatus.PASSWORD_REQUIRED);
        }
        // 암호화된 비밀번호는 BCrypt 해시 형태이므로 길이 제한을 늘림
        if (password.length() > 255) {
            throw new MemberHandler(MemberErrorStatus.PASSWORD_TOO_LONG);
        }
    }

    /**
     * 닉네임(이름) 변경
     */
    public void changeName(String newName) {
        validateName(newName);
        this.name = newName;
    }

    /**
     * 회원 활성 상태 확인
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * 회원 상태를 비활성으로 변경
     */
    public void deactivate() {
        if (!this.isActive) {
            throw new MemberHandler(MemberErrorStatus.ALREADY_INACTIVE);
        }
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 탈퇴 시간 조회
     */
    public LocalDateTime getDeletedAt() {
        return this.deletedAt;
    }

    /**
     * 비밀번호 검증
     * @param rawPassword 평문 비밀번호
     * @param passwordEncoder 비밀번호 인코더
     * @return 비밀번호 일치 여부
     */
    public boolean checkPassword(String rawPassword, com.ipia.order.common.util.PasswordEncoderUtil passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }

    /**
     * 테스트용 Member 생성 (Reflection 사용)
     * @param id 회원 ID
     * @param name 회원 이름
     * @param email 이메일
     * @param password 비밀번호 (암호화된 상태)
     * @param role 권한
     * @return 테스트용 Member 객체
     */
    public static Member createTestMember(Long id, String name, String email, String password, MemberRole role) {
        Member member = Member.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(role)
                .build();
        
        // Reflection을 사용하여 필드 설정
        try {
            // ID 필드 설정
            java.lang.reflect.Field idField = Member.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(member, id);
            
            // createdAt 필드 설정
            java.lang.reflect.Field createdAtField = Member.class.getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(member, LocalDateTime.now());
            
            // updatedAt 필드 설정
            java.lang.reflect.Field updatedAtField = Member.class.getSuperclass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(member, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("테스트용 Member 생성 실패", e);
        }
        
        return member;
    }

}
