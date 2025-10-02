package com.ipia.order.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.ipia.order.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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