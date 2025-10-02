package com.ipia.order.member.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.dao.DataIntegrityViolationException;

import com.ipia.order.member.domain.Member;

@DataJpaTest
@EnableJpaAuditing
@DisplayName("멤버 가입 테스트")
class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepostiory memberRepository;

    private Member validMember;

    @BeforeEach
    void setUp() {
        validMember = Member.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();
    }

    @Test
    @DisplayName("정상적인 멤버 가입 성공")
    void joinMember_Success() {
        // when
        Member savedMember = memberRepository.save(validMember);
        entityManager.flush();

        // then
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getName()).isEqualTo("홍길동");
        assertThat(savedMember.getEmail()).isEqualTo("hong@example.com");
        assertThat(savedMember.getCreatedAt()).isNotNull();
        assertThat(savedMember.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("중복 이메일로 인한 가입 실패")
    void joinMember_Fail_DuplicateEmail() {
        // given
        memberRepository.save(validMember);
        entityManager.flush();

        Member duplicateEmailMember = Member.builder()
                .name("김철수")
                .email("hong@example.com") // 동일한 이메일
                .build();

        // when & then
        assertThatThrownBy(() -> {
            memberRepository.save(duplicateEmailMember);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("이름이 null인 경우 가입 실패")
    void joinMember_Fail_NullName() {
        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name(null)
                    .email("test@example.com")
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("회원 이름은 필수입니다");
    }

    @Test
    @DisplayName("이름이 빈 문자열인 경우 가입 실패")
    void joinMember_Fail_EmptyName() {
        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name("")
                    .email("test@example.com")
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("회원 이름은 필수입니다");
    }

    @Test
    @DisplayName("이름이 공백만 있는 경우 가입 실패")
    void joinMember_Fail_BlankName() {
        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name("   ")
                    .email("test@example.com")
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("회원 이름은 필수입니다");
    }

    @Test
    @DisplayName("이름이 100자를 초과하는 경우 가입 실패")
    void joinMember_Fail_NameTooLong() {
        // given
        String longName = "a".repeat(101);

        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name(longName)
                    .email("test@example.com")
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("회원 이름은 100자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("이메일이 null인 경우 가입 실패")
    void joinMember_Fail_NullEmail() {
        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name("홍길동")
                    .email(null)
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("이메일은 필수입니다");
    }

    @Test
    @DisplayName("이메일이 빈 문자열인 경우 가입 실패")
    void joinMember_Fail_EmptyEmail() {
        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name("홍길동")
                    .email("")
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("이메일은 필수입니다");
    }

    @Test
    @DisplayName("이메일이 공백만 있는 경우 가입 실패")
    void joinMember_Fail_BlankEmail() {
        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name("홍길동")
                    .email("   ")
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("이메일은 필수입니다");
    }

    @Test
    @DisplayName("이메일이 200자를 초과하는 경우 가입 실패")
    void joinMember_Fail_EmailTooLong() {
        // given
        String longEmail = "a".repeat(190) + "@example.com"; // 200자 초과

        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name("홍길동")
                    .email(longEmail)
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("이메일은 200자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 가입 실패 - @ 없음")
    void joinMember_Fail_InvalidEmailFormat_NoAt() {
        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name("홍길동")
                    .email("invalid-email")
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("올바른 이메일 형식이 아닙니다");
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 가입 실패 - 도메인 없음")
    void joinMember_Fail_InvalidEmailFormat_NoDomain() {
        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name("홍길동")
                    .email("test@")
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("올바른 이메일 형식이 아닙니다");
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 가입 실패 - 확장자 없음")
    void joinMember_Fail_InvalidEmailFormat_NoExtension() {
        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name("홍길동")
                    .email("test@example")
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("올바른 이메일 형식이 아닙니다");
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 가입 실패 - 특수문자")
    void joinMember_Fail_InvalidEmailFormat_SpecialCharacters() {
        // when & then
        assertThatThrownBy(() -> {
            Member.builder()
                    .name("홍길동")
                    .email("test@example@com")
                    .build();
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("올바른 이메일 형식이 아닙니다");
    }

    @Test
    @DisplayName("정상적인 이메일 형식으로 가입 성공")
    void joinMember_Success_ValidEmailFormats() {
        // given
        String[] validEmails = {
                "test@example.com",
                "user.name@domain.co.kr",
                "user+tag@example.org",
                "123@test.co.kr"
        };

        for (int i = 0; i < validEmails.length; i++) {
            // when
            Member member = Member.builder()
                    .name("테스트" + i)
                    .email(validEmails[i])
                    .build();
            
            Member savedMember = memberRepository.save(member);
            entityManager.flush();
            entityManager.clear();

            // then
            assertThat(savedMember.getId()).isNotNull();
            assertThat(savedMember.getEmail()).isEqualTo(validEmails[i]);
        }
    }

    @Test
    @DisplayName("이름이 정확히 100자인 경우 가입 성공")
    void joinMember_Success_NameExactly100Characters() {
        // given
        String name100Chars = "a".repeat(100);

        // when
        Member member = Member.builder()
                .name(name100Chars)
                .email("test@example.com")
                .build();
        
        Member savedMember = memberRepository.save(member);
        entityManager.flush();

        // then
        assertThat(savedMember.getName()).isEqualTo(name100Chars);
    }

    @Test
    @DisplayName("이메일이 정확히 200자인 경우 가입 성공")
    void joinMember_Success_EmailExactly200Characters() {
        // given
        String email200Chars = "a".repeat(188) + "@example.com"; // 정확히 200자
        System.out.println("Email length: " + email200Chars.length());
        assertThat(email200Chars.length()).isEqualTo(200);

        // when
        Member member = Member.builder()
                .name("홍길동")
                .email(email200Chars)
                .build();
        
        Member savedMember = memberRepository.save(member);
        entityManager.flush();

        // then
        assertThat(savedMember.getEmail()).isEqualTo(email200Chars);
    }
}
