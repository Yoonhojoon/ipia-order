package com.ipia.order.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ipia.order.member.domain.Member;
import com.ipia.order.member.repository.MemberRepository;
import com.ipia.order.common.exception.member.MemberHandler;

@ExtendWith(MockitoExtension.class)
@DisplayName("멤버 서비스 테스트")
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

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
        void signup_Success() {
            // given
            String name = "홍길동";
            String email = "hong@example.com";
            
            given(memberRepository.save(any(Member.class)))
                    .willReturn(validMember);

            // when
            Member result = memberService.signup(name, email);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getEmail()).isEqualTo(email);
            
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("중복 이메일로 인한 가입 실패")
        void signup_Fail_DuplicateEmail() {
            // given
            String name = "홍길동";
            String email = "hong@example.com";

            given(memberRepository.existsByEmail(email))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.signup(name, email))
                    .isInstanceOf(MemberHandler.class);

            verify(memberRepository).existsByEmail(email);
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("Repository 저장 실패 시 예외 전파")
        void signup_Fail_RepositorySaveFailure() {
            // given
            String name = "홍길동";
            String email = "hong@example.com";
            
            given(memberRepository.save(any(Member.class)))
                    .willThrow(new RuntimeException("데이터베이스 저장 실패"));

            // when & then
            assertThatThrownBy(() -> memberService.signup(name, email))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("데이터베이스 저장 실패");
            
            verify(memberRepository).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("멤버 조회 테스트")
    class MemberFindTest {

        @Test
        @DisplayName("ID로 멤버 조회 성공")
        void findById_Success() {
            // given
            Long memberId = 1L;
            given(memberRepository.findById(memberId))
                    .willReturn(java.util.Optional.of(validMember));

            // when
            var result = memberService.findById(memberId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("홍길동");
            assertThat(result.get().getEmail()).isEqualTo("hong@example.com");
            
            verify(memberRepository).findById(memberId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 멤버 조회 시 빈 Optional 반환")
        void findById_Fail_NotFound() {
            // given
            Long nonExistentId = 999L;
            given(memberRepository.findById(nonExistentId))
                    .willReturn(java.util.Optional.empty());

            // when
            var result = memberService.findById(nonExistentId);

            // then
            assertThat(result).isEmpty();
            
            verify(memberRepository).findById(nonExistentId);
        }

        @Test
        @DisplayName("null ID로 멤버 조회 시 예외 발생")
        void findById_Fail_NullId() {
            // given
            Long nullId = null;
            given(memberRepository.findById(nullId))
                    .willThrow(new org.springframework.dao.InvalidDataAccessApiUsageException("The given id must not be null"));

            // when & then
            assertThatThrownBy(() -> memberService.findById(nullId))
                    .isInstanceOf(org.springframework.dao.InvalidDataAccessApiUsageException.class)
                    .hasMessageContaining("The given id must not be null");
            
            verify(memberRepository).findById(nullId);
        }

        @Test
        @DisplayName("이메일로 멤버 조회 성공")
        void findByEmail_Success() {
            // given
            String email = "hong@example.com";
            given(memberRepository.findByEmail(email))
                    .willReturn(java.util.Optional.of(validMember));

            // when
            var result = memberService.findByEmail(email);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("홍길동");
            assertThat(result.get().getEmail()).isEqualTo("hong@example.com");
            
            verify(memberRepository).findByEmail(email);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 멤버 조회 시 빈 Optional 반환")
        void findByEmail_Fail_NotFound() {
            // given
            String nonExistentEmail = "nonexistent@example.com";
            given(memberRepository.findByEmail(nonExistentEmail))
                    .willReturn(java.util.Optional.empty());

            // when
            var result = memberService.findByEmail(nonExistentEmail);

            // then
            assertThat(result).isEmpty();
            
            verify(memberRepository).findByEmail(nonExistentEmail);
        }

        @Test
        @DisplayName("null 이메일로 멤버 조회 시 빈 Optional 반환")
        void findByEmail_Fail_NullEmail() {
            // given
            given(memberRepository.findByEmail(null))
                    .willReturn(java.util.Optional.empty());

            // when
            var result = memberService.findByEmail(null);

            // then
            assertThat(result).isEmpty();
            
            verify(memberRepository).findByEmail(null);
        }

        @Test
        @DisplayName("빈 문자열 이메일로 멤버 조회 시 빈 Optional 반환")
        void findByEmail_Fail_EmptyEmail() {
            // given
            given(memberRepository.findByEmail(""))
                    .willReturn(java.util.Optional.empty());

            // when
            var result = memberService.findByEmail("");

            // then
            assertThat(result).isEmpty();
            
            verify(memberRepository).findByEmail("");
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
            
            java.util.List<Member> members = java.util.List.of(member1, member2);
            given(memberRepository.findAll())
                    .willReturn(members);

            // when
            var result = memberService.findAll();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Member::getName).containsExactlyInAnyOrder("홍길동", "김철수");
            assertThat(result).extracting(Member::getEmail).containsExactlyInAnyOrder("hong@example.com", "kim@example.com");
            
            verify(memberRepository).findAll();
        }

        @Test
        @DisplayName("멤버가 없을 때 모든 멤버 조회 시 빈 리스트 반환")
        void findAll_Empty() {
            // given
            given(memberRepository.findAll())
                    .willReturn(java.util.List.of());

            // when
            var result = memberService.findAll();

            // then
            assertThat(result).isEmpty();
            
            verify(memberRepository).findAll();
        }

        @Test
        @DisplayName("이름으로 멤버 조회 성공")
        void findByName_Success() {
            // given
            String name = "홍길동";
            Member member1 = Member.builder()
                    .name("홍길동")
                    .email("hong1@example.com")
                    .build();
            Member member2 = Member.builder()
                    .name("홍길동")
                    .email("hong2@example.com")
                    .build();
            
            java.util.List<Member> members = java.util.List.of(member1, member2);
            given(memberRepository.findByName(name))
                    .willReturn(members);

            // when
            var result = memberService.findByName(name);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Member::getName).containsOnly("홍길동");
            assertThat(result).extracting(Member::getEmail).containsExactlyInAnyOrder("hong1@example.com", "hong2@example.com");
            
            verify(memberRepository).findByName(name);
        }

        @Test
        @DisplayName("존재하지 않는 이름으로 멤버 조회 시 빈 리스트 반환")
        void findByName_Fail_NotFound() {
            // given
            String nonExistentName = "존재하지않는이름";
            given(memberRepository.findByName(nonExistentName))
                    .willReturn(java.util.List.of());

            // when
            var result = memberService.findByName(nonExistentName);

            // then
            assertThat(result).isEmpty();
            
            verify(memberRepository).findByName(nonExistentName);
        }

        @Test
        @DisplayName("null 이름으로 멤버 조회 시 빈 리스트 반환")
        void findByName_Fail_NullName() {
            // given
            given(memberRepository.findByName(null))
                    .willReturn(java.util.List.of());

            // when
            var result = memberService.findByName(null);

            // then
            assertThat(result).isEmpty();
            
            verify(memberRepository).findByName(null);
        }

        @Test
        @DisplayName("빈 문자열 이름으로 멤버 조회 시 빈 리스트 반환")
        void findByName_Fail_EmptyName() {
            // given
            given(memberRepository.findByName(""))
                    .willReturn(java.util.List.of());

            // when
            var result = memberService.findByName("");

            // then
            assertThat(result).isEmpty();
            
            verify(memberRepository).findByName("");
        }

        @Test
        @DisplayName("공백만 있는 이름으로 멤버 조회 시 빈 리스트 반환")
        void findByName_Fail_BlankName() {
            // given
            given(memberRepository.findByName("   "))
                    .willReturn(java.util.List.of());

            // when
            var result = memberService.findByName("   ");

            // then
            assertThat(result).isEmpty();
            
            verify(memberRepository).findByName("   ");
        }

        @Test
        @DisplayName("Repository 조회 실패 시 예외 전파 - findById")
        void findById_Fail_RepositoryFailure() {
            // given
            Long memberId = 1L;
            given(memberRepository.findById(memberId))
                    .willThrow(new RuntimeException("데이터베이스 조회 실패"));

            // when & then
            assertThatThrownBy(() -> memberService.findById(memberId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("데이터베이스 조회 실패");
            
            verify(memberRepository).findById(memberId);
        }

        @Test
        @DisplayName("Repository 조회 실패 시 예외 전파 - findByEmail")
        void findByEmail_Fail_RepositoryFailure() {
            // given
            String email = "hong@example.com";
            given(memberRepository.findByEmail(email))
                    .willThrow(new RuntimeException("데이터베이스 조회 실패"));

            // when & then
            assertThatThrownBy(() -> memberService.findByEmail(email))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("데이터베이스 조회 실패");
            
            verify(memberRepository).findByEmail(email);
        }

        @Test
        @DisplayName("Repository 조회 실패 시 예외 전파 - findAll")
        void findAll_Fail_RepositoryFailure() {
            // given
            given(memberRepository.findAll())
                    .willThrow(new RuntimeException("데이터베이스 조회 실패"));

            // when & then
            assertThatThrownBy(() -> memberService.findAll())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("데이터베이스 조회 실패");
            
            verify(memberRepository).findAll();
        }

        @Test
        @DisplayName("Repository 조회 실패 시 예외 전파 - findByName")
        void findByName_Fail_RepositoryFailure() {
            // given
            String name = "홍길동";
            given(memberRepository.findByName(name))
                    .willThrow(new RuntimeException("데이터베이스 조회 실패"));

            // when & then
            assertThatThrownBy(() -> memberService.findByName(name))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("데이터베이스 조회 실패");
            
            verify(memberRepository).findByName(name);
            }
        }
}