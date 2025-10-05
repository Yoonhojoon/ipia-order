package com.ipia.order.auth.service;

import com.ipia.order.auth.service.AuthService;
import com.ipia.order.common.exception.auth.AuthHandler;
import com.ipia.order.common.exception.auth.status.AuthErrorStatus;
import com.ipia.order.common.util.PasswordEncoderUtil;
import com.ipia.order.common.util.JwtUtil;
import com.ipia.order.common.enums.MemberRole;
import com.ipia.order.member.domain.Member;
import com.ipia.order.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServceImpl 테스트")
class AuthServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoderUtil passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("정상적인 이메일과 비밀번호로 로그인 성공")
        void 로그인_성공() {
            // Given
            String email = "test@example.com";
            String password = "password123";
            String encodedPassword = "encodedPassword";
            String expectedToken = "jwt-token";
            
            Member member = Member.createTestMember(1L, "홍길동", email, encodedPassword, MemberRole.USER);
            
            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
            given(member.checkPassword(password, passwordEncoder)).willReturn(true);
            given(jwtUtil.generateAccessToken(1L, email, "USER")).willReturn(expectedToken);

            // When
            String result = authService.login(email, password);

            // Then
            assertThat(result).isEqualTo(expectedToken);
            then(memberRepository).should(times(1)).findByEmail(email);
            then(jwtUtil).should(times(1)).generateAccessToken(1L, email, "USER");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        void 존재하지_않는_이메일_로그인_실패() {
            // Given
            String email = "nonexistent@example.com";
            String password = "password123";

            given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.MEMBER_NOT_FOUND);
                
            then(memberRepository).should(times(1)).findByEmail(email);
            then(jwtUtil).should(never()).generateAccessToken(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void 잘못된_비밀번호_로그인_실패() {
            // Given
            String email = "test@example.com";
            String password = "wrongpassword";
            String encodedPassword = "encodedPassword";
            
            Member member = Member.createTestMember(1L, "홍길동", email, encodedPassword, MemberRole.USER);
            
            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
            given(member.checkPassword(password, passwordEncoder)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.LOGIN_FAILED);
                
            then(memberRepository).should(times(1)).findByEmail(email);
            then(jwtUtil).should(never()).generateAccessToken(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("비활성화된 회원으로 로그인 실패")
        void 비활성화된_회원_로그인_실패() {
            // Given
            String email = "test@example.com";
            String password = "password123";
            String encodedPassword = "encodedPassword";
            
            Member member = Member.createTestMember(1L, "홍길동", email, encodedPassword, MemberRole.USER);
            member.deactivate(); // 비활성화
            
            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

            // When & Then
            assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.INACTIVE_MEMBER);
                
            then(memberRepository).should(times(1)).findByEmail(email);
            then(jwtUtil).should(never()).generateAccessToken(anyLong(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Test
        @DisplayName("유효한 토큰으로 로그아웃 성공")
        void 로그아웃_성공() {
            // Given
            String token = "valid-jwt-token";
            
            given(jwtUtil.validateToken(token)).willReturn(true);
            given(jwtUtil.isTokenExpired(token)).willReturn(false);
            given(jwtUtil.getTokenType(token)).willReturn("ACCESS");

            // When & Then - 예외가 발생하지 않아야 함
            assertThatCode(() -> authService.logout(token))
                .doesNotThrowAnyException();
                
            then(jwtUtil).should(times(1)).validateToken(token);
            then(jwtUtil).should(times(1)).isTokenExpired(token);
            then(jwtUtil).should(times(1)).getTokenType(token);
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 로그아웃 실패")
        void 유효하지_않은_토큰_로그아웃_실패() {
            // Given
            String invalidToken = "invalid-token";
            
            given(jwtUtil.validateToken(invalidToken)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.logout(invalidToken))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.INVALID_TOKEN);
                
            then(jwtUtil).should(times(1)).validateToken(invalidToken);
            then(jwtUtil).should(never()).isTokenExpired(anyString());
            then(jwtUtil).should(never()).getTokenType(anyString());
        }

        @Test
        @DisplayName("만료된 토큰으로 로그아웃 실패")
        void 만료된_토큰_로그아웃_실패() {
            // Given
            String expiredToken = "expired-jwt-token";
            
            given(jwtUtil.validateToken(expiredToken)).willReturn(true);
            given(jwtUtil.isTokenExpired(expiredToken)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.logout(expiredToken))
                .isInstanceOf(AuthHandler.class)
                .hasFieldOrPropertyWithValue("status", AuthErrorStatus.TOKEN_EXPIRED);
                
            then(jwtUtil).should(times(1)).validateToken(expiredToken);
            then(jwtUtil).should(times(1)).isTokenExpired(expiredToken);
            then(jwtUtil).should(never()).getTokenType(anyString());
        }
    }
}