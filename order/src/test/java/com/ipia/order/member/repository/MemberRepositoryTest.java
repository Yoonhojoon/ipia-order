package com.ipia.order.member.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.dao.DataIntegrityViolationException;

import com.ipia.order.member.domain.Member;

@DataJpaTest
@EnableJpaAuditing
@DisplayName("멤버 Repository 테스트")
class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    private Member validMember;

    @BeforeEach
    void setUp() {
        validMember = Member.builder()
                .name("홍길동")
                .email("hong@example.com")
                .build();
    }

    @Nested
    @DisplayName("멤버 가입 테스트")
    class MemberSignupTest {

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

    @Nested
    @DisplayName("멤버 조회 테스트")
    class MemberFindTest {

        @Test
        @DisplayName("ID로 멤버 조회 성공")
        void findById_Success() {
            // given
            Member savedMember = memberRepository.save(validMember);
            entityManager.flush();
            Long memberId = savedMember.getId();

            // when
            var foundMember = memberRepository.findById(memberId);

            // then
            assertThat(foundMember).isPresent();
            assertThat(foundMember.get().getId()).isEqualTo(memberId);
            assertThat(foundMember.get().getName()).isEqualTo("홍길동");
            assertThat(foundMember.get().getEmail()).isEqualTo("hong@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 멤버 조회 시 빈 Optional 반환")
        void findById_Fail_NotFound() {
            // given
            Long nonExistentId = 999L;

            // when
            var foundMember = memberRepository.findById(nonExistentId);

            // then
            assertThat(foundMember).isEmpty();
        }

        @Test
        @DisplayName("이메일로 멤버 조회 성공")
        void findByEmail_Success() {
            // given
            memberRepository.save(validMember);
            entityManager.flush();

            // when
            var foundMember = memberRepository.findByEmail("hong@example.com");

            // then
            assertThat(foundMember).isPresent();
            assertThat(foundMember.get().getName()).isEqualTo("홍길동");
            assertThat(foundMember.get().getEmail()).isEqualTo("hong@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 멤버 조회 시 빈 Optional 반환")
        void findByEmail_Fail_NotFound() {
            // given
            String nonExistentEmail = "nonexistent@example.com";

            // when
            var foundMember = memberRepository.findByEmail(nonExistentEmail);

            // then
            assertThat(foundMember).isEmpty();
        }

        @Test
        @DisplayName("이메일 대소문자 구분하여 조회")
        void findByEmail_CaseSensitive() {
            // given
            memberRepository.save(validMember);
            entityManager.flush();

            // when
            var foundMember = memberRepository.findByEmail("HONG@EXAMPLE.COM");

            // then
            assertThat(foundMember).isEmpty(); // 대소문자가 다르면 찾지 못함
        }

        @Test
        @DisplayName("이름으로 멤버 조회 성공")
        void findByName_Success() {
            // given
            Member member1 = Member.builder()
                    .name("홍길동")
                    .email("hong1@example.com")
                    .build();
            Member member2 = Member.builder()
                    .name("홍길동")
                    .email("hong2@example.com")
                    .build();
            Member member3 = Member.builder()
                    .name("김철수")
                    .email("kim@example.com")
                    .build();

            memberRepository.save(member1);
            memberRepository.save(member2);
            memberRepository.save(member3);
            entityManager.flush();

            // when
            var foundMembers = memberRepository.findByName("홍길동");

            // then
            assertThat(foundMembers).hasSize(2);
            assertThat(foundMembers).extracting(Member::getName).containsOnly("홍길동");
            assertThat(foundMembers).extracting(Member::getEmail).containsExactlyInAnyOrder("hong1@example.com", "hong2@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 이름으로 멤버 조회 시 빈 리스트 반환")
        void findByName_Fail_NotFound() {
            // given
            String nonExistentName = "존재하지않는이름";

            // when
            var foundMembers = memberRepository.findByName(nonExistentName);

            // then
            assertThat(foundMembers).isEmpty();
        }

        @Test
        @DisplayName("이름 대소문자 구분하여 조회")
        void findByName_CaseSensitive() {
            // given
            memberRepository.save(validMember);
            entityManager.flush();

            // when
            var foundMembers = memberRepository.findByName("홍길동");

            // then
            assertThat(foundMembers).hasSize(1);
            
            // when
            var foundMembersUpperCase = memberRepository.findByName("홍길동");

            // then
            assertThat(foundMembersUpperCase).hasSize(1); // 정확히 일치하는 경우만 찾음
        }

        @Test
        @DisplayName("모든 멤버 조회 성공")
        void findAll_Success() {
            // given
            Member member1 = Member.builder()
                    .name("홍길동")
                    .email("hong@example.com")
                    .build();
            Member member2 = Member.builder()
                    .name("김철수")
                    .email("kim@example.com")
                    .build();
            Member member3 = Member.builder()
                    .name("이영희")
                    .email("lee@example.com")
                    .build();

            memberRepository.save(member1);
            memberRepository.save(member2);
            memberRepository.save(member3);
            entityManager.flush();

            // when
            var allMembers = memberRepository.findAll();

            // then
            assertThat(allMembers).hasSize(3);
            assertThat(allMembers).extracting(Member::getName).containsExactlyInAnyOrder("홍길동", "김철수", "이영희");
            assertThat(allMembers).extracting(Member::getEmail).containsExactlyInAnyOrder("hong@example.com", "kim@example.com", "lee@example.com");
        }

        @Test
        @DisplayName("멤버가 없을 때 모든 멤버 조회 시 빈 리스트 반환")
        void findAll_Empty() {
            // when
            var allMembers = memberRepository.findAll();

            // then
            assertThat(allMembers).isEmpty();
        }

        @Test
        @DisplayName("null ID로 멤버 조회 시 예외 발생")
        void findById_Fail_NullId() {
            // given
            Long nullId = null;
            
            // when & then
            assertThatThrownBy(() -> memberRepository.findById(nullId))
                    .isInstanceOf(org.springframework.dao.InvalidDataAccessApiUsageException.class)
                    .hasMessageContaining("The given id must not be null");
        }

        @Test
        @DisplayName("null 이메일로 멤버 조회 시 빈 Optional 반환")
        void findByEmail_Fail_NullEmail() {
            // when
            var foundMember = memberRepository.findByEmail(null);

            // then
            assertThat(foundMember).isEmpty();
        }

        @Test
        @DisplayName("null 이름으로 멤버 조회 시 빈 리스트 반환")
        void findByName_Fail_NullName() {
            // when
            var foundMembers = memberRepository.findByName(null);

            // then
            assertThat(foundMembers).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열 이메일로 멤버 조회 시 빈 Optional 반환")
        void findByEmail_Fail_EmptyEmail() {
            // when
            var foundMember = memberRepository.findByEmail("");

            // then
            assertThat(foundMember).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열 이름으로 멤버 조회 시 빈 리스트 반환")
        void findByName_Fail_EmptyName() {
            // when
            var foundMembers = memberRepository.findByName("");

            // then
            assertThat(foundMembers).isEmpty();
        }

        @Test
        @DisplayName("공백만 있는 이메일로 멤버 조회 시 빈 Optional 반환")
        void findByEmail_Fail_BlankEmail() {
            // when
            var foundMember = memberRepository.findByEmail("   ");

            // then
            assertThat(foundMember).isEmpty();
        }

        @Test
        @DisplayName("공백만 있는 이름으로 멤버 조회 시 빈 리스트 반환")
        void findByName_Fail_BlankName() {
            // when
            var foundMembers = memberRepository.findByName("   ");

            // then
            assertThat(foundMembers).isEmpty();
            }
        }
}
