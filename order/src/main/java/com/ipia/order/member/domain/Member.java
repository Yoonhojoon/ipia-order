package com.ipia.order.member.domain;

import com.ipia.order.common.entity.BaseEntity;
import jakarta.persistence.*;
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

    @Builder
    private Member(String name, String email) {
        this.name = name;
        this.email = email;
    }

}
