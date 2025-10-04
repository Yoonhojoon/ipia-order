package com.ipia.order.member.domain;

import java.time.LocalDateTime;

import org.springframework.util.StringUtils;

import com.ipia.order.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Member(String name, String email) {
        validateName(name);
        validateEmail(email);
        this.name = name;
        this.email = email;
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("회원 이름은 필수입니다");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("회원 이름은 100자를 초과할 수 없습니다");
        }
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (email.length() > 200) {
            throw new IllegalArgumentException("이메일은 200자를 초과할 수 없습니다");
        }
        // 간단한 이메일 형식 검증
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
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
            throw new IllegalStateException("이미 탈퇴한 회원입니다");
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

}
