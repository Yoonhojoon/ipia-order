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
        @DisplayName("존재하지 않는 ID로 멤버 조회 시 MemberHandler 발생")
        void findById_Fail_NotFound() {
            // given
            Long nonExistentId = 999L;
            given(memberRepository.findById(nonExistentId))
                    .willReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.findById(nonExistentId))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository).findById(nonExistentId);
        }

        @Test
        @DisplayName("null ID로 멤버 조회 시 MemberHandler 발생 (레포 미호출)")
        void findById_Fail_NullId() {
            // given
            Long nullId = null;

            // when & then
            assertThatThrownBy(() -> memberService.findById(nullId))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository, never()).findById(any());
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
        @DisplayName("존재하지 않는 이메일로 멤버 조회 시 MemberHandler 발생")
        void findByEmail_Fail_NotFound() {
            // given
            String nonExistentEmail = "nonexistent@example.com";
            given(memberRepository.findByEmail(nonExistentEmail))
                    .willReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.findByEmail(nonExistentEmail))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository).findByEmail(nonExistentEmail);
        }

        @Test
        @DisplayName("null 이메일로 멤버 조회 시 MemberHandler 발생 (레포 미호출)")
        void findByEmail_Fail_NullEmail() {
            // when & then
            assertThatThrownBy(() -> memberService.findByEmail(null))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("빈 문자열 이메일로 멤버 조회 시 MemberHandler 발생 (레포 미호출)")
        void findByEmail_Fail_EmptyEmail() {
            // when & then
            assertThatThrownBy(() -> memberService.findByEmail(""))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository, never()).findByEmail(any());
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
        @DisplayName("null 이름으로 멤버 조회 시 MemberHandler 발생 (레포 미호출)")
        void findByName_Fail_NullName() {
            // when & then
            assertThatThrownBy(() -> memberService.findByName(null))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository, never()).findByName(any());
        }

        @Test
        @DisplayName("빈 문자열 이름으로 멤버 조회 시 MemberHandler 발생 (레포 미호출)")
        void findByName_Fail_EmptyName() {
            // when & then
            assertThatThrownBy(() -> memberService.findByName(""))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository, never()).findByName(any());
        }

        @Test
        @DisplayName("공백만 있는 이름으로 멤버 조회 시 MemberHandler 발생 (레포 미호출)")
        void findByName_Fail_BlankName() {
            // when & then
            assertThatThrownBy(() -> memberService.findByName("   "))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository, never()).findByName(any());
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

    @Nested
    @DisplayName("멤버 수정 테스트")
    class MemberUpdateTest {

        @Test
        @DisplayName("닉네임 변경 성공")
        void updateNickname_Success() {
            // given
            Long memberId = 1L;
            String newNickname = "새로운닉네임";
            Member existingMember = Member.builder()
                    .name("기존닉네임")
                    .email("test@example.com")
                    .build();
            
            given(memberRepository.findById(memberId))
                    .willReturn(java.util.Optional.of(existingMember));
            given(memberRepository.save(any(Member.class)))
                    .willReturn(existingMember);

            // when
            Member result = memberService.updateNickname(memberId, newNickname);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(newNickname);
            verify(memberRepository).findById(memberId);
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("닉네임 변경 - 존재하지 않는 ID 시 MemberHandler")
        void updateNickname_Fail_NotFound() {
            // given
            Long nonExistentId = 999L;
            given(memberRepository.findById(nonExistentId))
                    .willReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.updateNickname(nonExistentId, "새닉네임"))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository).findById(nonExistentId);
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("닉네임 변경 - null ID 시 MemberHandler")
        void updateNickname_Fail_NullId() {
            // when & then
            assertThatThrownBy(() -> memberService.updateNickname(null, "새닉네임"))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository, never()).findById(any());
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("닉네임 변경 - 공백 닉네임 시 MemberHandler")
        void updateNickname_Fail_BlankNickname() {
            // given
            Long memberId = 1L;
            given(memberRepository.findById(memberId))
                    .willReturn(java.util.Optional.of(validMember));

            // when & then
            assertThatThrownBy(() -> memberService.updateNickname(memberId, "   "))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository).findById(memberId);
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("비밀번호 변경 성공")
        void updatePassword_Success() {
            // given
            Long memberId = 1L;
            String currentPassword = "currentPass123!";
            String newPassword = "newPass123!";
            Member existingMember = Member.builder()
                    .name("홍길동")
                    .email("test@example.com")
                    .build();
            
            given(memberRepository.findById(memberId))
                    .willReturn(java.util.Optional.of(existingMember));
            given(memberRepository.save(any(Member.class)))
                    .willReturn(existingMember);

            // when
            memberService.updatePassword(memberId, currentPassword, newPassword);

            // then
            verify(memberRepository).findById(memberId);
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("비밀번호 변경 - 존재하지 않는 ID 시 MemberHandler")
        void updatePassword_Fail_NotFound() {
            // given
            Long nonExistentId = 999L;
            given(memberRepository.findById(nonExistentId))
                    .willReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.updatePassword(nonExistentId, "current", "newPass123!"))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository).findById(nonExistentId);
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("비밀번호 변경 - 현재 비밀번호 불일치 시 MemberHandler")
        void updatePassword_Fail_CurrentMismatch() {
            // given
            Long memberId = 1L;
            String wrongCurrentPassword = "wrongPassword";
            String newPassword = "newPass123!";
            given(memberRepository.findById(memberId))
                    .willReturn(java.util.Optional.of(validMember));

            // when & then
            assertThatThrownBy(() -> memberService.updatePassword(memberId, wrongCurrentPassword, newPassword))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository).findById(memberId);
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("비밀번호 변경 - 새 비밀번호 정책 위반")
        void updatePassword_Fail_NewPolicyViolation() {
            // given
            Long memberId = 1L;
            String currentPassword = "currentPass123!";
            String weakNewPassword = "123"; // 너무 짧음
            given(memberRepository.findById(memberId))
                    .willReturn(java.util.Optional.of(validMember));

            // when & then
            assertThatThrownBy(() -> memberService.updatePassword(memberId, currentPassword, weakNewPassword))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository).findById(memberId);
            verify(memberRepository, never()).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("멤버 탈퇴 테스트")
    class MemberWithdrawTest {

        @Test
        @DisplayName("정상적인 멤버 탈퇴 성공")
        void withdraw_Success() {
            // given
            Long memberId = 1L;
            Member activeMember = Member.builder()
                    .name("홍길동")
                    .email("hong@example.com")
                    .build();
            
            given(memberRepository.findById(memberId))
                    .willReturn(java.util.Optional.of(activeMember));
            given(memberRepository.save(any(Member.class)))
                    .willReturn(activeMember);

            // when
            memberService.withdraw(memberId);

            // then
            verify(memberRepository).findById(memberId);
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 탈퇴 시도 시 MemberHandler 발생")
        void withdraw_Fail_NotFound() {
            // given
            Long nonExistentId = 999L;
            given(memberRepository.findById(nonExistentId))
                    .willReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.withdraw(nonExistentId))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository).findById(nonExistentId);
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("null ID로 탈퇴 시도 시 MemberHandler 발생")
        void withdraw_Fail_NullId() {
            // when & then
            assertThatThrownBy(() -> memberService.withdraw(null))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository, never()).findById(any());
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("이미 탈퇴한 회원 탈퇴 시도 시 MemberHandler 발생")
        void withdraw_Fail_AlreadyInactive() {
            // given
            Long memberId = 1L;
            Member inactiveMember = Member.builder()
                    .name("홍길동")
                    .email("hong@example.com")
                    .build();
            // 이미 탈퇴한 상태로 설정
            inactiveMember.deactivate();
            
            given(memberRepository.findById(memberId))
                    .willReturn(java.util.Optional.of(inactiveMember));

            // when & then
            assertThatThrownBy(() -> memberService.withdraw(memberId))
                    .isInstanceOf(MemberHandler.class);
            
            verify(memberRepository).findById(memberId);
            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("Repository 저장 실패 시 예외 전파")
        void withdraw_Fail_RepositorySaveFailure() {
            // given
            Long memberId = 1L;
            Member activeMember = Member.builder()
                    .name("홍길동")
                    .email("hong@example.com")
                    .build();
            
            given(memberRepository.findById(memberId))
                    .willReturn(java.util.Optional.of(activeMember));
            given(memberRepository.save(any(Member.class)))
                    .willThrow(new RuntimeException("데이터베이스 저장 실패"));

            // when & then
            assertThatThrownBy(() -> memberService.withdraw(memberId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("데이터베이스 저장 실패");
            
            verify(memberRepository).findById(memberId);
            verify(memberRepository).save(any(Member.class));
        }
    }
}