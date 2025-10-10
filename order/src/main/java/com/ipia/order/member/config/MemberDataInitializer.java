package com.ipia.order.member.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ipia.order.member.domain.Member;
import com.ipia.order.member.enums.MemberRole;
import com.ipia.order.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("dev")
@Component
@Order(2)
@RequiredArgsConstructor
public class MemberDataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeAdmin();
        initializeDummyUsers();
    }

    private void initializeAdmin() {
        final String email = "admin@local.dev";
        if (memberRepository.existsByEmail(email)) {
            log.info("MEMBER:INIT:::admin already exists - {}", email);
            return;
        }
        Member admin = Member.builder()
                .name("Admin")
                .email(email)
                .password(passwordEncoder.encode("admin1234"))
                .role(MemberRole.ADMIN)
                .build();
        memberRepository.save(admin);
        log.info("MEMBER:INIT:::admin created - {}", email);
    }

    private void initializeDummyUsers() {
        String[][] users = {
                {"김철수", "user1@test.com", "user1pass"},
                {"이영희", "user2@test.com", "user2pass"},
                {"박민수", "user3@test.com", "user3pass"},
                {"정수진", "user4@test.com", "user4pass"},
                {"최지훈", "user5@test.com", "user5pass"}
        };
        for (String[] u : users) {
            String name = u[0];
            String email = u[1];
            String rawPw = u[2];

            if (memberRepository.existsByEmail(email)) {
                log.info("MEMBER:INIT:::user exists - {}", email);
                continue;
            }
            Member user = Member.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(rawPw))
                    .role(MemberRole.USER)
                    .build();
            memberRepository.save(user);
            log.info("MEMBER:INIT:::user created - {}", email);
        }
    }
}
